package com.example.sicenet_authprofile.data.local.repository

import com.example.sicenet_authprofile.data.local.dao.SicenetDao
import com.example.sicenet_authprofile.data.local.entities.CargaAcademicaEntity

class OfflineSicenetLocalRepository (
    private val sicenetDao: SicenetDao
) : SicenetLocalRepository {

    //CARGA ACADEMICA ---------------------------------------------------------
    override suspend fun saveCargaAcademica(matricula: String, json: String) {
        sicenetDao.insertCarga(CargaAcademicaEntity(matricula, json))
    }
    override suspend fun getCargaAcademica(matricula: String): CargaAcademicaEntity? {
        return sicenetDao.getAllCarga(matricula)
    }

}