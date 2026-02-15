package com.example.sicenet_authprofile.data.repository

import android.content.Context
import android.util.Log
import com.example.sicenet_authprofile.data.model.LoginResponse
import com.example.sicenet_authprofile.data.model.PerfilAcademico
import com.example.sicenet_authprofile.data.model.UserProfile
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.network.loginSoapTemplate
import com.example.sicenet_authprofile.data.network.profileSoapRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface SicenetRepository {
    suspend fun login(user: String, password: String): LoginResponse
    suspend fun getUserProfile(cookie: String): PerfilAcademico?
    fun clearSession()
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
                //Log.d("SICENET_Envio", "XML enviado: $soapBody")
                if (!responseString.trim().startsWith("<html>")) {
                    return if (result != null && result.contains("\"acceso\":true", ignoreCase = true)
                    ) {
                        LoginResponse(true, "Cookie stored by interceptor", "Login exitoso")
                    } else {
                        LoginResponse(false, null, "Credenciales incorrectas o error de servicio")
                    }
                }
                retryCount++;
                kotlinx.coroutines.delay(50)
            } catch (e: Exception) {
                retryCount++;
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
            println("body perfil: $jsonResult")
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

    override fun clearSession() {
        context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply() //.commit()
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




