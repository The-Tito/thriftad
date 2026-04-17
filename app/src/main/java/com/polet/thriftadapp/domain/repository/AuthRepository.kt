package com.polet.thriftadapp.domain.repository

import com.polet.thriftadapp.domain.model.LoginRequest
import com.polet.thriftadapp.domain.model.RegisterRequest
import com.polet.thriftadapp.domain.model.AuthResponse // <--- Importas el nuevo
import retrofit2.Response

interface AuthRepository {
    // Ahora ambos devuelven AuthResponse para que siempre tengamos el ID y el Rol a la mano
    suspend fun login(request: LoginRequest): Response<AuthResponse>
    suspend fun register(request: RegisterRequest): Response<AuthResponse>
}