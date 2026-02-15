package com.example.sicenet_authprofile.data.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// Interceptor que RECIBE la cookie del Login
class ReceivedCookiesInterceptor(private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())

        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val cookies = originalResponse.headers("Set-Cookie")
            val cookieBuilder = StringBuilder()

            for (cookie in cookies) {
                // Tomamos solo la parte importante antes del ";"
                val rawCookie = cookie.split(";")[0]
                if (cookieBuilder.isNotEmpty()) {
                    cookieBuilder.append("; ")
                }
                cookieBuilder.append(rawCookie)
            }

            val finalCookie = cookieBuilder.toString()
            Log.d("SICENET_SESSION", "Cookie recibida y guardada: $finalCookie")

            // Guardamos en nuestro SessionManager
            SessionManager.authCookie = finalCookie
        }
        return originalResponse
    }
}

// Interceptor que ENVÍA la cookie en cada petición
class AddCookiesInterceptor(private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        // Obtenemos la cookie del SessionManager
        val cookie = SessionManager.authCookie

        if (cookie != null) {
            builder.addHeader("Cookie", cookie)
        }

        // Headers obligatorios para Sicenet
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")

        return chain.proceed(builder.build())
    }
}