package com.example.sicenet_authprofile.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.sicenet_authprofile.R
import com.example.sicenet_authprofile.SicenetApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SicenetFetchWorker(
    ctx: Context,
    params: WorkerParameters
): CoroutineWorker(ctx, params){
    override suspend fun doWork(): Result {
        /*makeStatusNotification(
            applicationContext.resources.getString(R.string.iniciando_sesion),
            applicationContext
        )*/
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val application = applicationContext as SicenetApplication
                val repository = application.container.sicenetRepository

                // Datos de entrada
                val tipoSync = inputData.getString("TIPO_SYNC") ?: return@withContext Result.failure() //("PERFIL", "CARGA_ACADEMICA", etc)
                val lineamiento = inputData.getInt("LINEAMIENTO",0)
                val modEducatico = inputData.getInt("MOD_EDUCATIVO",1)

                //Descargar del json
                val jsonDescargado = when (tipoSync) {
                    "PERFIL" -> repository.getUserProfile()
                    "CARGA_ACADEMICA" -> repository.getCargaAcademica()
                    "CARDEX" -> repository.getCardex(lineamiento)
                    "CALIF_UNIDAD" -> repository.getCalificacionesUnidad()
                    "CALIF_FINAL" -> repository.getCalificacionesFinales(modEducatico)
                    else -> null
                }

                //Verificar que la descarag fue exitosa
                if (jsonDescargado.isNullOrEmpty()) {
                    return@withContext Result.failure()
                }

                //Convertir json a  archivo temporla en la memoria para no pasar el
                // limite de 10KB de workManager y mejor pasar la ruta
                val tempFile = File(applicationContext.cacheDir, "temp_$tipoSync.json")
                tempFile.writeText(jsonDescargado)

                //Pasar el json y la ruta del archivo temporalal siguiente worker
                val output = workDataOf(
                    "TIPO_SYNC" to tipoSync,
                    "FILE_PATH" to tempFile.absolutePath
                )
                Result.success(output)

            } catch (throwable: Throwable) {
                Result.retry()
            }
        }
    }
}