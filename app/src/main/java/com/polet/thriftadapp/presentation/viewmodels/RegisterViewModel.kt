package com.polet.thriftadapp.presentation.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.domain.model.RegisterRequest
import com.polet.thriftadapp.domain.repository.AuthRepository
import com.polet.thriftadapp.presentation.screens.login.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("thriftad_prefs", Context.MODE_PRIVATE)
    }

    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()

    fun onUserOrEmailChange(newValue: String) = _state.update { it.copy(userOrEmail = newValue, error = null) }
    fun onPasswordChange(newValue: String) = _state.update { it.copy(password = newValue, error = null) }
    fun onNombreCompletoChange(newValue: String) = _state.update { it.copy(nombreCompleto = newValue, error = null) }
    fun onRoleChange(newRole: String) = _state.update { it.copy(selectedRole = newRole) }
    fun togglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun toggleBiometric() = _state.update { it.copy(wantsBiometric = !it.wantsBiometric) }

    fun onRegisterClicked() {
        val user   = _state.value.userOrEmail.trim()
        val pass   = _state.value.password.trim()
        val role   = _state.value.selectedRole.lowercase()
        val nombre = _state.value.nombreCompleto.trim()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authRepository.register(RegisterRequest(user, pass, role, nombre))
                if (response.isSuccessful && response.body() != null) {
                    val registroData = response.body()!!

                    // Sesión persistente + datos para biométrico
                    prefs.edit()
                        .putInt("user_id",          registroData.id)
                        .putString("user_name",     registroData.username)
                        .putString("user_role",     registroData.role)
                        .putString("user_nombre",   registroData.nombreCompleto)
                        .putInt("biometric_user_id",     registroData.id)
                        .putString("biometric_username", registroData.username)
                        .putString("biometric_role",     registroData.role)
                        .putString("biometric_nombre",   registroData.nombreCompleto)
                        .apply()

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            userId    = registroData.id
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "El usuario ya existe o hubo un error") }
                }
            } catch (e: Exception) {
                android.util.Log.e("API_ERROR", e.message ?: "Error desconocido")
                _state.update { it.copy(isLoading = false, error = "Sin conexion al servidor") }
            }
        }
    }

    // Llamar desde RegisterScreen tras el BiometricPrompt exitoso
    fun saveBiometricEnabled() {
        prefs.edit().putBoolean("biometric_enabled", true).apply()
    }
}