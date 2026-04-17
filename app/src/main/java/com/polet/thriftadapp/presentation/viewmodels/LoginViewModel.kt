package com.polet.thriftadapp.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.domain.model.LoginRequest
import com.polet.thriftadapp.domain.repository.AuthRepository
import com.polet.thriftadapp.presentation.screens.login.LoginEvent
import com.polet.thriftadapp.presentation.screens.login.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs by lazy {
        context.getSharedPreferences("thriftad_prefs", Context.MODE_PRIVATE)
    }

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.UsuarioChanged -> {
                _uiState.update { it.copy(usuario = event.usuario, error = null) }
            }

            is LoginEvent.ContrasenaChanged -> {
                _uiState.update { it.copy(contrasena = event.contrasena, error = null) }
            }

            LoginEvent.EntrarClicked -> {
                validarYEntrar()
            }

            LoginEvent.BiometricLoginTriggered -> {
                onBiometricLoginSuccess()
            }
        }
    }

    private fun validarYEntrar() {
        // Quitamos espacios pero MANTENEMOS mayúsculas/minúsculas tal cual
        val user = _uiState.value.usuario.trim()
        val pass = _uiState.value.contrasena.trim()

        when {
            user.isBlank() -> {
                _uiState.update { it.copy(error = "El usuario no puede estar vacío") }
            }

            pass.isBlank() -> {
                _uiState.update { it.copy(error = "La contraseña es obligatoria") }
            }

            else -> {
                // ELIMINADO: .lowercase() - Ahora respetamos a Maria con M mayúscula
                login(user, pass)
            }
        }
    }

    private fun login(user: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val loginRequest = LoginRequest(
                    username = user,
                    password = pass
                )

                val response = authRepository.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!
                    // Sesión persistente
                    prefs.edit()
                        .putInt("user_id",          userData.id)
                        .putString("user_name",     userData.username)
                        .putString("user_role",     userData.role)
                        .putString("user_nombre",   userData.nombreCompleto)
                        // También actualizar los datos para biométrico
                        .putInt("biometric_user_id",  userData.id)
                        .putString("biometric_username", userData.username)
                        .putString("biometric_role",     userData.role)
                        .putString("biometric_nombre",   userData.nombreCompleto)
                        .apply()
                    _uiState.update {
                        it.copy(
                            isLoading       = false,
                            isLoginSuccess  = true,
                            role            = userData.role,
                            userId          = userData.id,
                            userName        = userData.username,
                            nombreCompleto  = userData.nombreCompleto,
                            usuario         = userData.username
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "Usuario o contraseña incorrectos")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error de conexión: Verifica tu servidor")
                }
            }
        }
    }

    fun logout() {
        prefs.edit()
            .remove("user_id")
            .remove("user_name")
            .remove("user_role")
            .remove("user_nombre")
            .remove("biometric_enabled")
            .remove("biometric_user_id")
            .remove("biometric_username")
            .remove("biometric_role")
            .remove("biometric_nombre")
            .apply()
        _uiState.value = LoginState()
    }

    // Biométrico verificado — restaurar sesión desde SharedPreferences
    fun onBiometricLoginSuccess() {
        val savedUserId = prefs.getInt("biometric_user_id", -1)
        val savedUsername = prefs.getString("biometric_username", null)
        val savedRole     = prefs.getString("biometric_role", "estudiante") ?: "estudiante"
        val savedNombre   = prefs.getString("biometric_nombre", "") ?: ""

        if (savedUserId != -1 && savedUsername != null) {
            _uiState.update {
                it.copy(
                    isLoading      = false,
                    isLoginSuccess = true,
                    userId         = savedUserId,
                    userName       = savedUsername,
                    role           = savedRole,
                    nombreCompleto = savedNombre,
                    error          = null
                )
            }
        } else {
            _uiState.update {
                it.copy(error = "No hay sesión guardada. Inicia sesión con usuario y contraseña primero.")
            }
        }
    }
}