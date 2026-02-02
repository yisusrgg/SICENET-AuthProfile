package com.example.sicenet_authprofile.data.network

import android.content.Context
import android.preference.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class AddCookiesInterceptor(private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        val preferences = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            .getStringSet("PREF_COOKIES", HashSet()) as HashSet<String>?
        
        preferences?.forEach { cookie ->
            builder.addHeader("Cookie", cookie)
        }
        return chain.proceed(builder.build())
    }
}

class ReceivedCookiesInterceptor(private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val cookies = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                .getStringSet("PREF_COOKIES", HashSet())?.toMutableSet() ?: mutableSetOf()
            
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies.add(header)
            }
            
            context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                .putStringSet("PREF_COOKIES", cookies)
                .apply()
        }
        return originalResponse
    }
}
