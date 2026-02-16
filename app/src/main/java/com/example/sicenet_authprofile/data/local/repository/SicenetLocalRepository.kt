package com.example.sicenet_authprofile.data.local.repository

import com.example.sicenet_authprofile.data.local.entities.CargaAcademicaEntity

interface SicenetLocalRepository {

    //CARGA ACADEMICA ----------------------------------------------------
    suspend fun saveCargaAcademica(matricula: String, json: String) // Métdo para guardar
    suspend fun getCargaAcademica(matricula: String): CargaAcademicaEntity?// Mtodos para leer
}