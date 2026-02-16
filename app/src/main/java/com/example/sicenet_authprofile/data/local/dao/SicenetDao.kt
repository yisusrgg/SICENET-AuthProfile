package com.example.sicenet_authprofile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sicenet_authprofile.data.local.entities.CargaAcademicaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SicenetDao {

    //CARGA ACADEMICA ------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarga(carga: CargaAcademicaEntity)

    //Traer la carga academica
    @Query("SELECT * FROM cargaAcademica WHERE matricula = :matricula")
    suspend fun getAllCarga(matricula : String): CargaAcademicaEntity?

}