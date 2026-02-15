package com.example.sicenet_authprofile.data.repository

import android.content.Context
import android.util.Log
import com.example.sicenet_authprofile.data.model.LoginResponse
import com.example.sicenet_authprofile.data.model.MateriaKardex
import com.example.sicenet_authprofile.data.model.PerfilAcademico
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.network.cardexRequest
import com.example.sicenet_authprofile.data.network.loginSoapTemplate
import com.example.sicenet_authprofile.data.network.profileSoapRequest
import com.example.sicenet_authprofile.data.network.SessionManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface SicenetRepository {
    suspend fun login(user: String, password: String): LoginResponse
    suspend fun getUserProfile(cookie: String): PerfilAcademico?
    fun clearSession()
    suspend fun getCargaAcademica(): String?
    suspend fun getKardex(lineamiento: String): List<MateriaKardex>? // Cambiado a List
    suspend fun getCalifUnidades(): String?
    suspend fun getCalifFinal(): String?
}

class SicenetRepositoryImpl(
    private val sicenetService: SicenetService,
    private val context: Context
) : SicenetRepository {

    override suspend fun login(user: String, password: String): LoginResponse {
        // Limpiamos sesión anterior al intentar login nuevo
        SessionManager.clear()

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
                        LoginResponse(true, SessionManager.authCookie, "Login exitoso")
                    } else {
                        LoginResponse(false, null, "Credenciales incorrectas")
                    }
                }
                retryCount++
                kotlinx.coroutines.delay(50)
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= max) return LoginResponse(false, null, e.message ?: "Error de red")
            }
        }
        return LoginResponse(false, null, "Error de servidor")
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
            e.printStackTrace()
            null
        }
    }

    override suspend fun getKardex(lineamiento: String): List<MateriaKardex>? {
        // Validación: Si el lineamiento está vacío, usa "1" por defecto para evitar error 500
        val lineamientoSeguro = if (lineamiento.trim().isEmpty()) "1" else lineamiento.trim()

        return try {
            val soapBody = cardexRequest.format(lineamientoSeguro)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())

            val response = sicenetService.getCardex(requestBody)
            val responseString = response.string()

            val jsonString = extractTagValue(responseString, "getAllKardexConPromedioByAlumnoResult")

            if (jsonString != null) {
                val listaMaterias = mutableListOf<MateriaKardex>()
                val jsonArray = org.json.JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    listaMaterias.add(
                        MateriaKardex(
                            clave = item.optString("clvMat"),
                            nombre = item.optString("materia"),
                            calificacion = item.optString("calif"),
                            semestre = item.optString("periodo")
                        )
                    )
                }
                listaMaterias
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e("SICENET_ERROR", "Error Kardex: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Métodos placeholder (vacíos por ahora)
    override suspend fun getCargaAcademica(): String? = null
    override suspend fun getCalifUnidades(): String? = null
    override suspend fun getCalifFinal(): String? = null

    override fun clearSession() {
        SessionManager.clear()
        context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .edit().clear().apply()
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