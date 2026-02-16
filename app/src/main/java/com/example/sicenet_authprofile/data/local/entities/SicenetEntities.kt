package com.example.sicenet_authprofile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// TABLA CARGA ACADÉMICA
@Entity(tableName = "cargaAcademica")
data class CargaAcademicaEntity(
    @PrimaryKey
    val matricula: String,
    val jsonContent: String,
    val lastUpdate: Long = System.currentTimeMillis()
)
