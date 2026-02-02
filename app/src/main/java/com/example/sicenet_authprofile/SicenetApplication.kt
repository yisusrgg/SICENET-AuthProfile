package com.example.sicenet_authprofile

import android.app.Application
import com.example.sicenet_authprofile.data.AppContainer
import com.example.sicenet_authprofile.data.DefaultAppContainer

class SicenetApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
