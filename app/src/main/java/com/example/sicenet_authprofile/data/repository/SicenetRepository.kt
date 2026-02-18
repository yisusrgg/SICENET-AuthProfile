package com.example.sicenet_authprofile.data.repository

import android.content.Context
import android.util.Log
import com.example.sicenet_authprofile.data.local.*
import com.example.sicenet_authprofile.data.model.*
import com.example.sicenet_authprofile.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

interface SicenetRepository {
    // Auth
    suspend fun login(user: String, password: String): LoginResponse
    fun clearSession()

    // Local Data Flows
    fun getProfileFromDb(): Flow<PerfilAcademico?>
    fun getCalificacionesFinalesFromDb(): Flow<List<CalificacionFinal>>
    fun getCalificacionesUnidadFromDb(): Flow<List<CalificacionUnidad>>
    fun getCardexFromDb(): Flow<List<CardexItem>>
    fun getCargaAcademicaFromDb(): Flow<List<Materia>>

    // Remote Refresh Methods
    suspend fun refreshProfile(cookie: String)
    suspend fun refreshCalificacionesFinales(modEducativo: Int)
    suspend fun refreshCalificacionesUnidad()
    suspend fun refreshCardex(lineamiento: Int)
    suspend fun refreshCargaAcademica()

    // One-shot Remote Fetch Methods (Added to fix ViewModel errors)
    suspend fun getUserProfile(cookie: String): PerfilAcademico?
    suspend fun getCardex(lineamiento: Int): List<CardexItem>
    suspend fun getCalificacionesFinales(modEducativo: Int): List<CalificacionFinal>
    suspend fun getCalificacionesUnidad(): List<CalificacionUnidad>
    suspend fun getCargaAcademica(): List<Materia>
}

class SicenetRepositoryImpl(
    private val sicenetService: SicenetService,
    private val sicenetDao: SicenetDao,
    private val context: Context
) : SicenetRepository {

    // --- Local Data Flows implementation ---

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

    // --- Remote Refresh Methods implementation ---

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

    override suspend fun refreshProfile(cookie: String) {
        getUserProfile(cookie)
    }

    override suspend fun getUserProfile(cookie: String): PerfilAcademico? {
        try {
            val requestBody = profileSoapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getProfile(requestBody)
            val jsonResult = extractTagValue(response.string(), "getAlumnoAcademicoWithLineamientoResult")
            if (jsonResult != null) {
                val json = JSONObject(jsonResult)
                val profile = PerfilAcademico(
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
                val entity = PerfilEntity(
                    matricula = profile.matricula,
                    nombre = profile.nombre,
                    carrera = profile.carrera,
                    especialidad = profile.especialidad,
                    semActual = profile.semActual,
                    cdtosAcumulados = profile.cdtosAcumulados,
                    cdtosActuales = profile.cdtosActuales,
                    estatus = profile.estatus,
                    inscrito = profile.inscrito,
                    adeudo = profile.adeudo,
                    fechaReins = profile.fechaReins,
                    modEducativo = profile.modEducativo,
                    urlFoto = profile.urlFoto,
                    adeudoDescripcion = profile.adeudoDescripcion,
                    lineamiento = profile.lineamiento
                )
                sicenetDao.insertPerfil(entity)
                return profile
            }
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getUserProfile: ${e.message}")
        }
        return null
    }

    override suspend fun refreshCalificacionesFinales(modEducativo: Int) {
        getCalificacionesFinales(modEducativo)
    }

    override suspend fun getCalificacionesFinales(modEducativo: Int): List<CalificacionFinal> {
        try {
            val soapBody = califFinalRequest.format(modEducativo)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCalifFinal(requestBody)
            val jsonResult = extractTagValue(response.string(), "getAllCalifFinalByAlumnosResult")
            if (jsonResult != null) {
                val jsonArray = JSONArray(jsonResult)
                val list = mutableListOf<CalificacionFinal>()
                val entities = mutableListOf<CalificacionFinalEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val materia = obj.optString("Materia", obj.optString("materia"))
                    val calif = obj.optString("Calif", obj.optString("calif"))
                    list.add(CalificacionFinal(materia, calif))
                    entities.add(CalificacionFinalEntity(materia = materia, calificacion = calif))
                }
                sicenetDao.deleteCalificacionesFinales()
                sicenetDao.insertCalificacionesFinales(entities)
                return list
            }
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCalificacionesFinales: ${e.message}")
        }
        return emptyList()
    }

    override suspend fun refreshCalificacionesUnidad() {
        getCalificacionesUnidad()
    }

    override suspend fun getCalificacionesUnidad(): List<CalificacionUnidad> {
        try {
            val requestBody = califUnidadRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCalifUnidad(requestBody)
            val jsonResult = extractTagValue(response.string(), "getCalifUnidadesByAlumnoResult")
            if (jsonResult != null) {
                val jsonArray = JSONArray(jsonResult)
                val list = mutableListOf<CalificacionUnidad>()
                val entities = mutableListOf<CalificacionUnidadEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val unidades = mutableListOf<String>()
                    val materia = obj.optString("Materia")
                    val unidadesActivas = obj.optString("UnidadesActivas", "")
                    for (u in 1..unidadesActivas.length) {
                        val cal = obj.optString("C$u")
                        unidades.add(if (cal == "null" || cal.isEmpty()) "0" else cal)
                    }
                    val validGrades = unidades.mapNotNull { it.toIntOrNull() }
                    val promedio = if (validGrades.isNotEmpty()) validGrades.average().toInt().toString() else "0"
                    
                    list.add(CalificacionUnidad(materia, unidades, promedio))
                    entities.add(CalificacionUnidadEntity(materia = materia, unidades = unidades, promedio = promedio))
                }
                sicenetDao.deleteCalificacionesUnidades()
                sicenetDao.insertCalificacionesUnidades(entities)
                return list
            }
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCalificacionesUnidad: ${e.message}")
        }
        return emptyList()
    }

    override suspend fun refreshCardex(lineamiento: Int) {
        getCardex(lineamiento)
    }

    override suspend fun getCardex(lineamiento: Int): List<CardexItem> {
        try {
            val soapBody = cardexRequest.format(lineamiento)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCardex(requestBody)
            val jsonResult = extractTagValue(response.string(), "getAllKardexConPromedioByAlumnoResult")
            if (jsonResult != null) {
                val jsonObject = JSONObject(jsonResult)
                val jsonArray = jsonObject.optJSONArray("lstKardex") ?: return emptyList()
                val list = mutableListOf<CardexItem>()
                val entities = mutableListOf<CardexEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val item = CardexItem(
                        materia = obj.optString("Materia"),
                        calificacion = obj.optString("Calif"),
                        semestre = obj.optString("S1"),
                        creditos = obj.optString("Cdts"),
                        estatus = obj.optString("Acred")
                    )
                    list.add(item)
                    entities.add(CardexEntity(
                        materia = item.materia,
                        calificacion = item.calificacion,
                        semestre = item.semestre,
                        creditos = item.creditos,
                        estatus = item.estatus
                    ))
                }
                sicenetDao.deleteCardex()
                sicenetDao.insertCardex(entities)
                return list
            }
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCardex: ${e.message}")
        }
        return emptyList()
    }

    override suspend fun refreshCargaAcademica() {
        getCargaAcademica()
    }

    override suspend fun getCargaAcademica(): List<Materia> {
        try {
            val requestBody = cargaAcademicaRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCargaAcademica(requestBody)
            val jsonResult = extractTagValue(response.string(), "getCargaAcademicaByAlumnoResult")
            if (jsonResult != null) {
                val jsonArray = JSONArray(jsonResult)
                val list = mutableListOf<Materia>()
                val entities = mutableListOf<MateriaEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val materia = Materia(
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
                    list.add(materia)
                    entities.add(MateriaEntity(
                        clvOficial = materia.clvOficial,
                        docente = materia.docente,
                        materia = materia.materia,
                        grupo = materia.grupo,
                        creditosMateria = materia.creditosMateria,
                        estadoMateria = materia.estadoMateria,
                        lunes = materia.lunes,
                        martes = materia.martes,
                        miercoles = materia.miercoles,
                        jueves = materia.jueves,
                        viernes = materia.viernes,
                        sabado = materia.sabado
                    ))
                }
                sicenetDao.deleteCargaAcademica()
                sicenetDao.insertCargaAcademica(entities)
                return list
            }
        } catch (e: Exception) {
            Log.e("SICENET_REPO", "Error getCargaAcademica: ${e.message}")
        }
        return emptyList()
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
