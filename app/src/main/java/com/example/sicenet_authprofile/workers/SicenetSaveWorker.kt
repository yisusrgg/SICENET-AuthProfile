package com.example.sicenet_authprofile.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sicenet_authprofile.R
import com.example.sicenet_authprofile.SicenetApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SicenetSaveWorker(
    ctx: Context,
    params: WorkerParameters
): CoroutineWorker(ctx, params){
    override suspend fun doWork(): Result {
        /*makeStatusNotification(
            applicationContext.resources.getString(R.string.guardar_datos),
            applicationContext
        )*/
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val application = applicationContext as SicenetApplication
                val repository = application.container.sicenetRepository

                //Datos de entrada (JSON y tipo que nos mandó el FetchPerfilWorker)
                val jsonData = inputData.getString("JSON_DATA") ?: return@withContext Result.failure()
                val tipoSync = inputData.getString("TIPO_SYNC") ?: return@withContext Result.failure()

                //Guardar en la base de datos local
                when(tipoSync){
                    "PERFIL" -> repository.saveUserPerfilDb(jsonData)
                    "CARDEX" -> repository.saveCardexDb(jsonData)
                    "CARGA_ACADEMICA" -> repository.saveCargaAcademicaDb(jsonData)
                    "CALIF_UNIDAD" -> repository.saveCalificacionesUnidadDb(jsonData)
                    "CALIF_FINAL" -> repository.saveCalificacionesFinalesDb(jsonData)
                }

                //Guardar la fecha de actualización
                val prefs = applicationContext.getSharedPreferences("SICENET_PREFS", Context.MODE_PRIVATE)
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                prefs.edit().putString("FECHA_ACT_$tipoSync", fecha).apply()

                Result.success()

            } catch (throwable: Throwable) {
                Result.failure()
            }
        }
    }
}