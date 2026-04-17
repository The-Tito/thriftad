package com.polet.thriftadapp.domain.model

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: String,
    val nombreCompleto: String = ""
)