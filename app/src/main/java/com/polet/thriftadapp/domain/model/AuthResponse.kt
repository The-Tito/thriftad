package com.polet.thriftadapp.domain.model

data class AuthResponse(
    val id: Int,
    val username: String,
    val role: String,
    val nombreCompleto: String = "",
    val message: String? = null
)