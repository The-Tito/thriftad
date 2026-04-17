package com.polet.thriftadapp.presentation.screens.profile

/**
 * Representa el estado actual de la configuración del usuario.
 * Esta clase es el "único punto de verdad" para la UI.
 */
data class AccountState(
    // Casilla híbrida: Puede ser "Polet Jimenez" o "polet@ejemplo.com"
    val identifier: String = "Polet Itandegui Jimenez Diaz",

    // Rol actual del usuario
    val role: String = "Estudiante", // Opciones fijas: "Estudiante", "Negocio", "Hogar"

    // Preferencias de hardware y sistema
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true
)