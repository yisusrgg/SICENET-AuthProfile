package com.example.sicenet_authprofile.data.repository

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.sicenet_authprofile.data.local.CalificacionFinalEntity
import com.example.sicenet_authprofile.data.local.CalificacionUnidadEntity
import com.example.sicenet_authprofile.data.local.CardexEntity
import com.example.sicenet_authprofile.data.local.MateriaEntity
import com.example.sicenet_authprofile.data.local.PerfilEntity
import com.example.sicenet_authprofile.data.local.SicenetDao
import com.example.sicenet_authprofile.data.model.CalificacionFinal
import com.example.sicenet_authprofile.data.model.CalificacionUnidad
import com.example.sicenet_authprofile.data.model.CardexItem
import com.example.sicenet_authprofile.data.model.LoginResponse
import com.example.sicenet_authprofile.data.model.Materia
import com.example.sicenet_authprofile.data.model.PerfilAcademico
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.network.califFinalRequest
import com.example.sicenet_authprofile.data.network.califUnidadRequest
import com.example.sicenet_authprofile.data.network.cardexRequest
import com.example.sicenet_authprofile.data.network.cargaAcademicaRequest
import com.example.sicenet_authprofile.data.network.loginSoapTemplate
import com.example.sicenet_authprofile.data.network.profileSoapRequest
import com.example.sicenet_authprofile.workers.SicenetFetchWorker
import com.example.sicenet_authprofile.workers.SicenetSaveWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class SicenetRepositoryImpl (
    private val sicenetService: SicenetService,
    private val sicenetDao: SicenetDao,
    private val context: Context
) : SicenetRepository {

    // WorkManager
    private val workManager = WorkManager.getInstance(context)

    override fun sincronizarDato(tipoSync: String, lineamiento: Int, modEducativo: Int) {
        val input = workDataOf(
            "TIPO_SYNC" to tipoSync,
            "LINEAMIENTO" to lineamiento,
            "MOD_EDUCATIVO" to modEducativo
        )

        // Restricciones (Tener internet) ----
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // WorkRequests ----
        val fetchRequest = OneTimeWorkRequestBuilder<SicenetFetchWorker>()
            .setInputData(input)
            .setConstraints(constraints)
            .build()

        val saveRequest = OneTimeWorkRequestBuilder<SicenetSaveWorker>().build()

        // Iniciar el trabajo(work) ----
        workManager.beginUniqueWork( //garantiza que solo haya 1 sincronizacion exitiendo
            "Sync_$tipoSync",
            ExistingWorkPolicy.REPLACE,
            fetchRequest
        )
            .then(saveRequest)
            .enqueue()
    }

    // --- Local Data Flows implementation ---------------
    //Las usa ViewModel
    override fun getProfileFromDb(): Flow<PerfilAcademico?> = sicenetDao.getPerfil().map { entity ->
        entity?.let {
            PerfilAcademico(
                fechaReins = it.fechaReins,
                modEducativo = it.modEducativo,
                adeudo = it.adeudo,
                urlFoto = it.urlFoto,
                adeudoDescripcion = it.adeudoDescripcion,
                inscrito = it.inscrito,
                estatus = it.estatus,
                semActual = it.semActual,
                cdtosAcumulados = it.cdtosAcumulados,
                cdtosActuales = it.cdtosActuales,
                especialidad = it.especialidad,
                carrera = it.carrera,
                lineamiento = it.lineamiento,
                nombre = it.nombre,
                matricula = it.matricula
            )
        }
    }

    override fun getCalificacionesFinalesFromDb(): Flow<List<CalificacionFinal>> =
        sicenetDao.getCalificacionesFinales().map { list ->
            list.map { CalificacionFinal(it.materia, it.calificacion) }
        }

    override fun getCalificacionesUnidadFromDb(): Flow<List<CalificacionUnidad>> =
        sicenetDao.getCalificacionesUnidades().map { list ->
            list.map { CalificacionUnidad(it.materia, it.unidades, it.promedio) }
        }

    override fun getCardexFromDb(): Flow<List<CardexItem>> =
        sicenetDao.getCardex().map { list ->
            list.map { CardexItem(it.materia, it.calificacion, it.semestre, it.creditos, it.estatus) }
        }

    override fun getCargaAcademicaFromDb(): Flow<List<Materia>> =
        sicenetDao.getCargaAcademica().map { list ->
            list.map {
                Materia(
                    it.docente, it.clvOficial, it.estadoMateria, it.creditosMateria,
                    it.materia, it.grupo, it.lunes, it.martes, it.miercoles,
                    it.jueves, it.viernes, it.sabado
                )
            }
        }

    // --- Remote Refresh Methods implementation ---------------------------------
    //Las usa workManager(FetchWorker)
    override suspend fun login(user: String, password: String): LoginResponse {
        var retryCount = 0
        val max = 3
        while (max > retryCount) {
            try {
                val soapBody = loginSoapTemplate.format(user, password)
                val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
                val response = sicenetService.login(requestBody)
                val responseString = response.string()
                val result = extractTagValue(responseString, "accesoLoginResult")
                if (!responseString.trim().startsWith("<html>")) {
                    return if (result != null && result.contains("\"acceso\":true", ignoreCase = true)) {
                        sicenetDao.clearAllData() // Limpiar datos previos al iniciar sesión
                        LoginResponse(true, "Cookie stored by interceptor", "Login exitoso")
                    } else {
                        LoginResponse(false, null, "Credenciales incorrectas o error de servicio")
                    }
                }
                retryCount++
                kotlinx.coroutines.delay(50)
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= max) return LoginResponse(false, null, e.message ?: "Error de red")
            }
        }
        return LoginResponse(false, null, "El servidor no responde correctamente")
    }


    override suspend fun getUserProfile(): String? {
        return try {
            val requestBody = profileSoapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getProfile(requestBody)
            val jsonResult = extractTagValue(response.string(), "getAlumnoAcademicoWithLineamientoResult")
            jsonResult
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getUserProfile: ${e.message}")
            null
        }
    }

    override suspend fun getCalificacionesFinales(modEducativo: Int): String?{
        return try {
            val soapBody = califFinalRequest.format(modEducativo)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCalifFinal(requestBody)
            val jsonResult = extractTagValue(response.string(), "getAllCalifFinalByAlumnosResult")
            jsonResult
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCalificacionesFinales: ${e.message}")
            null
        }
    }

    override suspend fun getCalificacionesUnidad(): String? {
        return try {
            val requestBody = califUnidadRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCalifUnidad(requestBody)
            val jsonResult = extractTagValue(response.string(), "getCalifUnidadesByAlumnoResult")
            jsonResult
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCalificacionesUnidad: ${e.message}")
            null
        }
    }

    override suspend fun getCardex(lineamiento: Int): String? {
        return try {
            val soapBody = cardexRequest.format(lineamiento)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCardex(requestBody)
            val jsonResult = extractTagValue(response.string(), "getAllKardexConPromedioByAlumnoResult")
            Log.d("KARDEX", jsonResult.toString())
            return jsonResult
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCardex: ${e.message}")
            null
        }
    }

    override suspend fun getCargaAcademica(): String? {
        return try {
            val requestBody = cargaAcademicaRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCargaAcademica(requestBody)
            val jsonResult = extractTagValue(response.string(), "getCargaAcademicaByAlumnoResult")
            return jsonResult
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCargaAcademica: ${e.message}")
            null
        }
    }


    //Metodos para guardar la repsuesta de intenet a la base de datos --------------------
    //Las usa workaMager (SaveWorker)
    override suspend fun saveUserPerfilDb(jsonString: String) {
        if (jsonString.isEmpty()) return

        val json = JSONObject(jsonString)
        val entity = PerfilEntity(
            matricula = json.optString("matricula"),
            nombre = json.optString("nombre"),
            carrera = json.optString("carrera"),
            especialidad = json.optString("especialidad"),
            semActual = json.optInt("semActual"),
            cdtosAcumulados = json.optInt("cdtosAcumulados"),
            cdtosActuales = json.optInt("cdtosActuales"),
            estatus = json.optString("estatus"),
            inscrito = json.optBoolean("inscrito"),
            adeudo = json.optBoolean("adeudo"),
            fechaReins = json.optString("fechaReins"),
            modEducativo = json.optInt("modEducativo"),
            urlFoto = json.optString("urlFoto"),
            adeudoDescripcion = json.optString("adeudoDescripcion"),
            lineamiento = json.optInt("lineamiento")
        )
        sicenetDao.insertPerfil(entity)
    }

    override suspend fun saveCardexDb(jsonString: String) {
        if (jsonString.isEmpty()) return

        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.optJSONArray("lstKardex") ?: return
        val entities = mutableListOf<CardexEntity>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            entities.add(
                CardexEntity(
                    materia = obj.optString("Materia"),
                    calificacion = obj.optString("Calif"),
                    semestre = obj.optString("S1"),
                    creditos = obj.optString("Cdts"),
                    estatus = obj.optString("Acred")
                )
            )
        }
        // Borramos lo viejo y guardamos lo nuevo
        sicenetDao.deleteCardex()
        sicenetDao.insertCardex(entities)
    }

    override suspend fun saveCargaAcademicaDb(jsonString: String){
        if(jsonString.isEmpty()) return

        val jsonArray = JSONArray(jsonString)
        val entities = mutableListOf<MateriaEntity>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            entities.add(
                MateriaEntity(
                    clvOficial = obj.optString("clvOficial"),
                    docente = obj.optString("Docente"),
                    materia = obj.optString("Materia"),
                    grupo = obj.optString("Grupo"),
                    creditosMateria = obj.optInt("CreditosMateria"),
                    estadoMateria = obj.optString("EstadoMateria"),
                    lunes = obj.optString("Lunes"),
                    martes = obj.optString("Martes"),
                    miercoles = obj.optString("Miercoles"),
                    jueves = obj.optString("Jueves"),
                    viernes = obj.optString("Viernes"),
                    sabado = obj.optString("Sabado")
                )
            )
        }
        sicenetDao.deleteCargaAcademica()
        sicenetDao.insertCargaAcademica(entities)
    }

    override suspend fun saveCalificacionesFinalesDb(jsonString: String) {
        if (jsonString.isEmpty()) return

        val jsonArray = JSONArray(jsonString)
        val entities = mutableListOf<CalificacionFinalEntity>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val materia = obj.optString("Materia", obj.optString("materia"))
            val calif = obj.optString("Calif", obj.optString("calif"))
            entities.add(
                CalificacionFinalEntity(
                    materia = materia,
                    calificacion = calif)
            )
        }
        sicenetDao.deleteCalificacionesFinales()
        sicenetDao.insertCalificacionesFinales(entities)
    }

    override suspend fun saveCalificacionesUnidadDb(jsonString: String) {
        if (jsonString.isEmpty())  return

        val jsonArray = JSONArray(jsonString)
        val entities = mutableListOf<CalificacionUnidadEntity>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val unidades = mutableListOf<String>()
            val unidadesActivas = obj.optString("UnidadesActivas", "")
            for (u in 1..unidadesActivas.length) {
                val cal = obj.optString("C$u")
                unidades.add(if (cal == "null" || cal.isEmpty()) "0" else cal)
            }
            val validGrades = unidades.mapNotNull { it.toIntOrNull() }
            val promedio = if (validGrades.isNotEmpty()) validGrades.average().toInt().toString() else "0"

            entities.add(
                CalificacionUnidadEntity(
                    materia = obj.optString("Materia"),
                    unidades = unidades,
                    promedio = promedio
                )
            )
        }
        sicenetDao.deleteCalificacionesUnidades()
        sicenetDao.insertCalificacionesUnidades(entities)
    }


    override fun clearSession() {
        context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun extractTagValue(xml: String, tag: String): String? {
        val openTag = "<$tag>"
        val closeTag = "</$tag>"
        val startIndex = xml.indexOf(openTag)
        val endIndex = xml.indexOf(closeTag)
        return if (startIndex != -1 && endIndex != -1) xml.substring(startIndex + openTag.length, endIndex) else null
    }
}
