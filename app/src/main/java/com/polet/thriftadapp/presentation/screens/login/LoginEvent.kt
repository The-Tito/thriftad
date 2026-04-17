package com.polet.thriftadapp.presentation.screens.login

// Usamos sealed interface para que sea un conjunto cerrado de acciones
sealed interface LoginEvent {

    // Cuando el usuario escribe en el campo de texto del usuario
    data class UsuarioChanged(val usuario: String) : LoginEvent

    // Cuando el usuario escribe en el campo de la contraseña
    data class ContrasenaChanged(val contrasena: String) : LoginEvent

    // Cuando el usuario presiona el botón morado de "ENTRAR"
    data object EntrarClicked : LoginEvent

    // NUEVO: Para el Plan B de la huella
    data object BiometricLoginTriggered : LoginEvent
}