package com.example.sicenet_authprofile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfil_academico")
data class PerfilEntity(
    @PrimaryKey val matricula: String,
    val nombre: String,
    val carrera: String,
    val especialidad: String,
    val semActual: Int,
    val cdtosAcumulados: Int,
    val cdtosActuales: Int,
    val estatus: String,
    val inscrito: Boolean,
    val adeudo: Boolean,
    val fechaReins: String,
    val modEducativo: Int,
    val urlFoto: String,
    val adeudoDescripcion: String,
    val lineamiento: Int
)

@Entity(tableName = "calificaciones_finales")
data class CalificacionFinalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val calificacion: String
)

@Entity(tableName = "calificaciones_unidades")
data class CalificacionUnidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val unidades: List<String>,
    val promedio: String
)

@Entity(tableName = "cardex")
data class CardexEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materia: String,
    val calificacion: String,
    val semestre: String,
    val creditos: String,
    val estatus: String
)

@Entity(tableName = "carga_academica")
data class MateriaEntity(
    @PrimaryKey val clvOficial: String,
    val docente: String,
    val estadoMateria: String,
    val creditosMateria: Int,
    val materia: String,
    val grupo: String,
    val lunes: String,
    val martes: String,
    val miercoles: String,
    val jueves: String,
    val viernes: String,
    val sabado: String
)
