package com.polet.thriftadapp.domain.model

data class LoginRequest(
    val username: String,
    val password: String
)