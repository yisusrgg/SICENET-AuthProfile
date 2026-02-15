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
data class PerfilAcademico(
    val fechaReins: String,
    val modEducativo: Int,
    val adeudo: Boolean,
    val urlFoto: String,
    val adeudoDescripcion: String,
    val inscrito: Boolean,
    val estatus: String,
    val semActual: Int,
    val cdtosAcumulados: Int,
    val cdtosActuales: Int,
    val especialidad: String,
    val carrera: String,
    val lineamiento: Int,
    val nombre: String,
    val matricula: String
)


