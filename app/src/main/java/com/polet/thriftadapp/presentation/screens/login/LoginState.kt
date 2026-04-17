package com.polet.thriftadapp.presentation.screens.login

data class LoginState(
    val usuario: String = "",
    val contrasena: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val error: String? = null,
    val role: String = "estudiante",
    val userId: Int = -1,
    val userName: String = "",
    val nombreCompleto: String = ""
)