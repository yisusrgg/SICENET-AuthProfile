package com.example.sicenet_authprofile.data

import android.content.Context
import com.example.sicenet_authprofile.data.local.SicenetDatabase
import com.example.sicenet_authprofile.data.local.repository.OfflineSicenetLocalRepository
import com.example.sicenet_authprofile.data.local.repository.SicenetLocalRepository
import com.example.sicenet_authprofile.data.network.AddCookiesInterceptor
import com.example.sicenet_authprofile.data.network.ReceivedCookiesInterceptor
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.repository.SicenetRepository
import com.example.sicenet_authprofile.data.repository.SicenetRepositoryImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

interface AppContainer {
    val sicenetRepository: SicenetRepository
    val sicenetLocalRepository: SicenetLocalRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx/"

    //Configuración del cliente OkHttp con tus Interceptores de Cookies
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor(context))
        .addInterceptor(ReceivedCookiesInterceptor(context))
        .build()

    //Configuración de Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    //Crear el servicio
    private val retrofitService: SicenetService by lazy {
        retrofit.create(SicenetService::class.java)
    }

    //Inicializar la Base de datos local (Room)
    private val database: SicenetDatabase by lazy {
        SicenetDatabase.getDatabase(context)
    }

    //Implementación del Repositorio Remoto
    override val sicenetRepository: SicenetRepository by lazy {
        SicenetRepositoryImpl(retrofitService, context)
    }

    //Implementación del Repositorio Local
    override val sicenetLocalRepository: SicenetLocalRepository by lazy {
        OfflineSicenetLocalRepository(database.sicenetDao())
    }
}