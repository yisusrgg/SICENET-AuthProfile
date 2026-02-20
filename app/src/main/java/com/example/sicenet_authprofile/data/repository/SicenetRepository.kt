package com.example.sicenet_authprofile.data.repository

import com.example.sicenet_authprofile.data.model.*
import kotlinx.coroutines.flow.Flow


interface SicenetRepository {
    // Auth
    suspend fun login(user: String, password: String): LoginResponse
    fun sincronizarDato(tipoSync: String, lineamiento: Int, modEducativo: Int)
    fun clearSession()

    // Local Data Flows
    fun getProfileFromDb(): Flow<PerfilAcademico?>
    fun getCardexFromDb(): Flow<List<CardexItem>>
    fun getCargaAcademicaFromDb(): Flow<List<Materia>>
    fun getCalificacionesFinalesFromDb(): Flow<List<CalificacionFinal>>
    fun getCalificacionesUnidadFromDb(): Flow<List<CalificacionUnidad>>

    // One-shot Remote Fetch Methods
    suspend fun getUserProfile(): String?
    suspend fun getCardex(lineamiento: Int): String?
    suspend fun getCalificacionesFinales(modEducativo: Int): String?
    suspend fun getCalificacionesUnidad(): String?
    suspend fun getCargaAcademica(): String?

    //Guardar en la base de datos local para el segundo wroker
    suspend fun saveUserPerfilDb(jsonString : String)
    suspend fun saveCardexDb(jsonString : String)
    suspend fun saveCargaAcademicaDb(jsonString : String)
    suspend fun saveCalificacionesFinalesDb(jsonString : String)
    suspend fun saveCalificacionesUnidadDb(jsonString : String)
}
