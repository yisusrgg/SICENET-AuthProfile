package com.example.sicenet_authprofile.data.repository

import android.content.Context
import android.util.Log
import com.example.sicenet_authprofile.data.model.CalificacionFinal
import com.example.sicenet_authprofile.data.model.CalificacionUnidad
import com.example.sicenet_authprofile.data.model.LoginResponse
import com.example.sicenet_authprofile.data.model.PerfilAcademico
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.network.califFinalRequest
import com.example.sicenet_authprofile.data.network.califUnidadRequest
import com.example.sicenet_authprofile.data.network.cargaAcademicaRequest
import com.example.sicenet_authprofile.data.network.loginSoapTemplate
import com.example.sicenet_authprofile.data.network.profileSoapRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

import retrofit2.HttpException

interface SicenetRepository {
    suspend fun login(user: String, password: String): LoginResponse
    suspend fun getUserProfile(cookie: String): PerfilAcademico?
    fun clearSession()
    suspend fun getCargaAcademica(): String?
    suspend fun getCalificacionesFinales(modEducativo: Int): List<CalificacionFinal>
    suspend fun getCalificacionesUnidad(): List<CalificacionUnidad>
}

class SicenetRepositoryImpl(
    private val sicenetService: SicenetService,
    private val context: Context
) : SicenetRepository {

    override suspend fun login(user: String, password: String): LoginResponse {
        var retryCount = 0
        val max = 3
        while (max > retryCount) {
            try{
                val soapBody = loginSoapTemplate.format(user, password)
                val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
                val response = sicenetService.login(requestBody)
                val responseString = response.string()
                val result = extractTagValue(responseString, "accesoLoginResult")
                if (!responseString.trim().startsWith("<html>")) {
                    return if (result != null && result.contains("\"acceso\":true", ignoreCase = true)
                    ) {
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
        return LoginResponse(false, null, "El servidor no responde correctamente tras varios intentos")
    }

    override suspend fun getUserProfile(cookie: String): PerfilAcademico? {
        return try {
            val requestBody = profileSoapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getProfile(requestBody)
            val responseString = response.string()
            
            val jsonResult = extractTagValue(responseString, "getAlumnoAcademicoWithLineamientoResult")
            if (jsonResult != null) {
                val json = JSONObject(jsonResult)
                PerfilAcademico(
                    fechaReins = json.optString("fechaReins"),
                    modEducativo = json.optInt("modEducativo"),
                    adeudo = json.optBoolean("adeudo"),
                    urlFoto = json.optString("urlFoto"),
                    adeudoDescripcion = json.optString("adeudoDescripcion"),
                    inscrito = json.optBoolean("inscrito"),
                    estatus = json.optString("estatus"),
                    semActual = json.optInt("semActual"),
                    cdtosAcumulados = json.optInt("cdtosAcumulados"),
                    cdtosActuales = json.optInt("cdtosActuales"),
                    especialidad = json.optString("especialidad"),
                    carrera = json.optString("carrera"),
                    lineamiento = json.optInt("lineamiento"),
                    nombre = json.optString("nombre"),
                    matricula = json.optString("matricula")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCargaAcademica(): String? {
        return try {
            val requestBody = cargaAcademicaRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCargaAcademica(requestBody)
            val responseString = response.string()
            Log.d("SICENET_RES", "Respuesta Servidor (Carga): $responseString")
            val jsonResult = extractTagValue(responseString, "getCargaAcademicaByAlumnoResult")
            jsonResult

        } catch (e: HttpException) {
            null
        }
    }

    override suspend fun getCalificacionesFinales(modEducativo: Int): List<CalificacionFinal> {
        return try {
            val soapBody = califFinalRequest.format(modEducativo)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCalifFinal(requestBody)
            val responseString = response.string()
            Log.d("SICENET_RES", "Respuesta Servidor (Final): $responseString")
            val jsonResult = extractTagValue(responseString, "getAllCalifFinalByAlumnosResult")
            
            if (jsonResult != null) {
                val jsonArray = JSONArray(jsonResult)
                val list = mutableListOf<CalificacionFinal>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    // SICENET keys are often capitalized: Materia, Calif or calif
                    list.add(CalificacionFinal(
                        materia = obj.optString("Materia", obj.optString("materia")),
                        calificacion = obj.optString("Calif", obj.optString("calif"))
                    ))
                }
                list
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCalificacionesUnidad(): List<CalificacionUnidad> {
        return try {
            val requestBody = califUnidadRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getCalifUnidad(requestBody)
            val responseString = response.string()
            Log.d("SICENET_RES", "Respuesta Servidor (Unidad): $responseString")
            val jsonResult = extractTagValue(responseString, "getCalifUnidadesByAlumnoResult")
            
            if (jsonResult != null) {
                val jsonArray = JSONArray(jsonResult)
                val list = mutableListOf<CalificacionUnidad>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val unidades = mutableListOf<String>()
                    
                    // The JSON shows keys Materia, UnidadesActivas, and C1, C2...
                    val materia = obj.optString("Materia")
                    val unidadesActivas = obj.optString("UnidadesActivas", "")
                    
                    for (u in 1..unidadesActivas.length) {
                        val cal = obj.optString("C$u")
                        // If null or empty, we use "0" as default
                        if (cal == "null" || cal.isEmpty()) {
                            unidades.add("0")
                        } else {
                            unidades.add(cal)
                        }
                    }
                    
                    // Calculate promedio if possible
                    val validGrades = unidades.mapNotNull { it.toIntOrNull() }
                    val promedio = if (validGrades.isNotEmpty()) {
                        validGrades.average().toInt().toString()
                    } else "0"

                    list.add(CalificacionUnidad(
                        materia = materia,
                        unidades = unidades,
                        promedio = promedio
                    ))
                }
                list
            } else emptyList()
        } catch (e: Exception) {
            Log.e("SICENET_ERROR", "Error parsing unidad: ${e.message}")
            emptyList()
        }
    }

    override fun clearSession() {
        context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply() 
    }

    private fun extractTagValue(xml: String, tag: String): String? {
        val openTag = "<$tag>"
        val closeTag = "</$tag>"
        val startIndex = xml.indexOf(openTag)
        val endIndex = xml.indexOf(closeTag)
        
        return if (startIndex != -1 && endIndex != -1) {
            xml.substring(startIndex + openTag.length, endIndex)
        } else {
            null
        }
    }
}
