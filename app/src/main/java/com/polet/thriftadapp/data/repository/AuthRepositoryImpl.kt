package com.polet.thriftadapp.data.repository

import com.polet.thriftadapp.data.remote.ApiService
import com.polet.thriftadapp.domain.model.AuthResponse // <--- ESTE IMPORT ES VITAL
import com.polet.thriftadapp.domain.model.LoginRequest
import com.polet.thriftadapp.domain.model.RegisterRequest
import com.polet.thriftadapp.domain.repository.AuthRepository
import retrofit2.Response
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {

    // 1. Ahora coincide con la interfaz entregando AuthResponse
    override suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return apiService.login(request)
    }

    // 2. Ahora coincide con la interfaz entregando AuthResponse
    override suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return apiService.register(request)
    }
}