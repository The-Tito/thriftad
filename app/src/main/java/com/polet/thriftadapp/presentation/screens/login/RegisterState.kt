package com.polet.thriftadapp.presentation.screens.login

data class RegisterState(
    val userOrEmail: String = "",
    val password: String = "",
    val nombreCompleto: String = "",
    val selectedRole: String = "Estudiante",
    val isPasswordVisible: Boolean = false,
    val wantsBiometric: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userId: Int = -1
)