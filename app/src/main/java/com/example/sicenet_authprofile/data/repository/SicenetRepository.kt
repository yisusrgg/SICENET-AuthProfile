package com.example.sicenet_authprofile.data.repository

import com.example.sicenet_authprofile.data.model.LoginResponse
import com.example.sicenet_authprofile.data.model.UserProfile
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.network.loginSoapTemplate
import com.example.sicenet_authprofile.data.network.profileSoapRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface SicenetRepository {
    suspend fun login(user: String, password: String): LoginResponse
    suspend fun getUserProfile(cookie: String): UserProfile?
}

class SicenetRepositoryImpl(private val sicenetService: SicenetService) : SicenetRepository {

    override suspend fun login(user: String, password: String): LoginResponse {
        return try {
            val soapBody = loginSoapTemplate.format(user, password)
            val requestBody = soapBody.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.login(requestBody)
            val responseString = response.string()

            val result = extractTagValue(responseString, "accesoLoginResult")

            if (result != null && result.contains("\"acceso\":true", ignoreCase = true)) {
                LoginResponse(true, "Cookie stored by interceptor", "Login exitoso")
            } else {
                LoginResponse(false, null, "Credenciales incorrectas o error de servicio")
            }
        } catch (e: Exception) {
            LoginResponse(false, null, e.message ?: "Error de red")
        }
    }

    override suspend fun getUserProfile(cookie: String): UserProfile? {
        return try {
            val requestBody = profileSoapRequest.toRequestBody("text/xml; charset=utf-8".toMediaType())
            val response = sicenetService.getProfile(requestBody)
            val responseString = response.string()
            
            val jsonResult = extractTagValue(responseString, "getAlumnoAcademicoWithLineamientoResult")

            if (jsonResult != null) {
                val json = JSONObject(jsonResult)
                UserProfile(
                    nombre = json.optString("nombre", ""),
                    matricula = json.optString("matricula", ""),
                    carrera = json.optString("carrera", ""),
                    situacion = json.optString("situacion", ""),
                    promedio = json.optString("promedio", "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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


