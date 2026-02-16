package com.example.sicenet_authprofile.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SicenetDao {
    // Perfil
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfil(perfil: PerfilEntity)

    @Query("SELECT * FROM perfil_academico LIMIT 1")
    fun getPerfil(): Flow<PerfilEntity?>

    @Query("DELETE FROM perfil_academico")
    suspend fun deletePerfil()

    // Calificaciones Finales
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificacionesFinales(calificaciones: List<CalificacionFinalEntity>)

    @Query("SELECT * FROM calificaciones_finales")
    fun getCalificacionesFinales(): Flow<List<CalificacionFinalEntity>>

    @Query("DELETE FROM calificaciones_finales")
    suspend fun deleteCalificacionesFinales()

    // Calificaciones Unidades
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalificacionesUnidades(calificaciones: List<CalificacionUnidadEntity>)

    @Query("SELECT * FROM calificaciones_unidades")
    fun getCalificacionesUnidades(): Flow<List<CalificacionUnidadEntity>>

    @Query("DELETE FROM calificaciones_unidades")
    suspend fun deleteCalificacionesUnidades()

    // Cardex
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardex(items: List<CardexEntity>)

    @Query("SELECT * FROM cardex")
    fun getCardex(): Flow<List<CardexEntity>>

    @Query("DELETE FROM cardex")
    suspend fun deleteCardex()

    // Carga Académica (Materias)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCargaAcademica(materias: List<MateriaEntity>)

    @Query("SELECT * FROM carga_academica")
    fun getCargaAcademica(): Flow<List<MateriaEntity>>

    @Query("DELETE FROM carga_academica")
    suspend fun deleteCargaAcademica()

    @Transaction
    suspend fun clearAllData() {
        deletePerfil()
        deleteCalificacionesFinales()
        deleteCalificacionesUnidades()
        deleteCardex()
        deleteCargaAcademica()
    }
}
