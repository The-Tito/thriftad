package com.polet.thriftadapp.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.presentation.screens.add.AddTransactionEvent
import com.polet.thriftadapp.presentation.screens.add.categoriasPorRol
import com.polet.thriftadapp.presentation.screens.add.AddTransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor() : ViewModel() {

    var state by mutableStateOf(AddTransactionState())
        private set

    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            is AddTransactionEvent.UserIdSet -> {
                state = state.copy(userId = event.userId)
            }
            is AddTransactionEvent.RoleSet -> {
                val nuevasCategorias = categoriasPorRol(event.role)
                state = state.copy(
                    role = event.role,
                    categoriasDisponibles = nuevasCategorias,
                    categoria = nuevasCategorias.first()
                )
            }
            is AddTransactionEvent.NombreChanged -> {
                state = state.copy(nombre = event.value, error = null)
            }
            is AddTransactionEvent.MontoChanged -> {
                state = state.copy(monto = event.value, error = null)
            }
            is AddTransactionEvent.ToggleEsIngreso -> {
                state = state.copy(esIngreso = event.value, error = null)
            }
            is AddTransactionEvent.FechaChanged -> {
                state = state.copy(fecha = event.value)
            }
            is AddTransactionEvent.ToggleCategoriaMenu -> {
                state = state.copy(isCategoriaExpanded = !state.isCategoriaExpanded)
            }
            is AddTransactionEvent.CategoriaSelected -> {
                state = state.copy(categoria = event.value, isCategoriaExpanded = false)
            }
            is AddTransactionEvent.ToggleUnidadMenu -> {
                state = state.copy(isUnidadExpanded = !state.isUnidadExpanded)
            }
            is AddTransactionEvent.UnidadSelected -> {
                state = state.copy(unidad = event.value, isUnidadExpanded = false)
            }
            is AddTransactionEvent.CantidadChanged -> {
                state = state.copy(cantidad = event.value)
            }
            is AddTransactionEvent.DescripcionChanged -> {
                state = state.copy(descripcion = event.value)
            }
            AddTransactionEvent.SaveTransaction -> saveTransaction()
            AddTransactionEvent.ResetSaved -> {
                state = state.copy(isSaved = false)
            }
        }
    }

    private fun saveTransaction() {
        val monto = state.monto.toDoubleOrNull()
        if (monto == null || monto <= 0.0) {
            state = state.copy(error = "El monto no es válido")
            return
        }
        if (state.nombre.isBlank()) {
            state = state.copy(error = "El nombre no puede estar vacío")
            return
        }

        // Solo validar y marcar como listo para navegar a Camera.
        // El guardado real (Room + API) ocurre en CameraScreen al confirmar con foto,
        // evitando así el registro doble (AddTransaction + Camera).
        state = state.copy(isSaved = true, error = null)
    }
}