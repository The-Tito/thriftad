package com.polet.thriftadapp.domain.model

data class User(
    val id: Int = 0,
    val username: String,
    val password: String, // Aquí viajará la contraseña (el backend la hashea)
    val role: String = "estudiante"
)