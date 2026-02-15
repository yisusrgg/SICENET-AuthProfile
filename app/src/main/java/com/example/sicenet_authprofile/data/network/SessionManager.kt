package com.example.sicenet_authprofile.data.network

object SessionManager {
    var authCookie: String? = null

    fun hasSession(): Boolean = !authCookie.isNullOrEmpty()

    fun clear() {
        authCookie = null
    }
}