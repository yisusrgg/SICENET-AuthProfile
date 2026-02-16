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

data class CalificacionFinal(
    val materia: String = "",
    val calificacion: String = ""
)
data class Materia(
    val docente: String,
    val clvOficial: String,
    val estadoMateria: String,
    val creditosMateria: Int,
    val materia: String,
    val grupo: String,

    // Horario
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String,
    val sabado: String
)

data class CalificacionUnidad(
    val materia: String = "",
    val unidades: List<String> = emptyList(),
    val promedio: String = ""
)

data class CardexItem(
    val materia: String,
    val calificacion: String,
    val semestre: String,
    val creditos: String,
    val estatus: String // Ej: Acreditada
)
