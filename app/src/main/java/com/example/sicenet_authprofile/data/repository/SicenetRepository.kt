package com.example.sicenet_authprofile.data.repository

import com.example.sicenet_authprofile.data.model.LoginResponse
import com.example.sicenet_authprofile.data.model.UserProfile

interface SicenetRepository {
    suspend fun login(user: String, password: String): LoginResponse
    suspend fun getUserProfile(cookie: String): UserProfile?
}

class SicenetRepositoryImpl : SicenetRepository {
    // This will be implemented with OkHttp when SOAP details are provided
    override suspend fun login(user: String, password: String): LoginResponse {
        // Placeholder
        return LoginResponse(false, null, "Not implemented yet")
    }

    override suspend fun getUserProfile(cookie: String): UserProfile? {
        // Placeholder
        return null
    }
}
