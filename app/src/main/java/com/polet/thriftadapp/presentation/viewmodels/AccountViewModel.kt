package com.polet.thriftadapp.presentation.viewmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.core.util.HapticManager
import com.polet.thriftadapp.data.remote.ApiService
import com.polet.thriftadapp.presentation.screens.profile.AccountState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val hapticManager: HapticManager,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("thriftad_prefs", Context.MODE_PRIVATE)
    }

    var state by mutableStateOf(AccountState())
        private set

    init {
        // Cargar los valores guardados al abrir la pantalla
        state = AccountState(
            identifier        = prefs.getString("user_nombre", "")
                                    ?.takeIf { it.isNotBlank() }
                                ?: prefs.getString("user_name", "") ?: "",
            role              = prefs.getString("user_role", "estudiante")
                                    ?.replaceFirstChar { it.uppercaseChar() } ?: "Estudiante",
            isVibrationEnabled = prefs.getBoolean("pref_vibration", true),
            isSoundEnabled     = prefs.getBoolean("pref_sound", true)
        )
    }

    fun onIdentifierChange(newValue: String) {
        state = state.copy(identifier = newValue)
        // Guardamos inmediatamente; la clave depende de si es email o nombre
        val key = if (newValue.contains("@")) "user_name" else "user_nombre"
        prefs.edit().putString(key, newValue).apply()
    }

    fun onRoleChange(newRole: String) {
        state = state.copy(role = newRole)
        val roleLower = newRole.lowercase()
        prefs.edit().putString("user_role", roleLower).apply()

        // Sincronizar el nuevo rol con el backend
        val userId = prefs.getInt("user_id", -1)
        if (userId != -1) {
            viewModelScope.launch {
                try {
                    apiService.updateRole(userId, roleLower)
                } catch (_: Exception) {
                    // Sin conexión: el rol queda guardado en prefs y se aplicará al re-login
                }
            }
        }
    }

    fun onVibrationToggle(enabled: Boolean) {
        state = state.copy(isVibrationEnabled = enabled)
        prefs.edit().putBoolean("pref_vibration", enabled).apply()
        if (enabled) hapticManager.executeSuccess()
    }

    fun onSoundToggle(enabled: Boolean) {
        state = state.copy(isSoundEnabled = enabled)
        prefs.edit().putBoolean("pref_sound", enabled).apply()
    }
}
