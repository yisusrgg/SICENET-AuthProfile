package com.example.sicenet_authprofile.data.model

data class LoginResponse(
    val success: Boolean,
    val cookie: String? = null,
    val message: String? = null
)
data class UserProfile(
    val nombre: String = "",
    val matricula: String = "",
    val carrera: String = "",
    val situacion: String = "",
    val promedio: String = ""
)
