package com.example.sicenet_authprofile.data

import android.content.Context
import com.example.sicenet_authprofile.data.network.AddCookiesInterceptor
import com.example.sicenet_authprofile.data.network.ReceivedCookiesInterceptor
import com.example.sicenet_authprofile.data.network.SicenetService
import com.example.sicenet_authprofile.data.repository.SicenetRepository
import com.example.sicenet_authprofile.data.repository.SicenetRepositoryImpl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

interface AppContainer {
    val sicenetRepository: SicenetRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val baseUrl = "https://sicenet.surguanajuato.tecnm.mx/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(AddCookiesInterceptor(context))
        .addInterceptor(ReceivedCookiesInterceptor(context))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(SimpleXmlConverterFactory.createNonStrict())
        .client(client)
        .build()

    private val retrofitService: SicenetService by lazy {
        retrofit.create(SicenetService::class.java)
    }

    override val sicenetRepository: SicenetRepository by lazy {
        SicenetRepositoryImpl(retrofitService, context)
    }
}
