package com.polet.thriftadapp.presentation.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.remote.ApiService
import com.polet.thriftadapp.domain.model.MovimientoRequest
import com.polet.thriftadapp.presentation.screens.camera.CameraEvent
import com.polet.thriftadapp.presentation.screens.camera.CameraState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val apiService: ApiService,
    private val ticketDao: TicketDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var state by mutableStateOf(CameraState())
        private set

    private var pendingTicketId: Int = -1

    fun setTicketId(id: Int) { pendingTicketId = id }

    fun onEvent(event: CameraEvent) {
        when (event) {
            is CameraEvent.UserIdSet         -> state = state.copy(userId = event.userId)
            is CameraEvent.CapturePhoto      -> state = state.copy(isPhotoTaken = true)
            is CameraEvent.OnPhotoTaken      -> state = state.copy(capturedImageUri = event.uri)
            is CameraEvent.CancelAndRepeat   -> state = state.copy(isPhotoTaken = false, capturedImageUri = null)
            is CameraEvent.OnConceptChange   -> state = state.copy(concept = event.concept)
            is CameraEvent.OnAmountChange    -> state = state.copy(amount = event.amount)
            is CameraEvent.OnCategoriaChange -> state = state.copy(categoria = event.categoria)
            is CameraEvent.OnFechaChange     -> state = state.copy(fecha = event.fecha)
            is CameraEvent.ConfirmAndSave    -> {
                val concepto = if (event.concept.isNotBlank()) event.concept else state.concept
                val monto    = if (event.amount.isNotBlank()) event.amount    else state.amount
                sincronizarConBackend(concepto, monto)
            }
            is CameraEvent.EditInformation   -> { /* Navegación handled in NavGraph */ }
        }
    }

    // POST directo al backend. Room insert es responsabilidad de PlacesViewModel (tiene GPS).
    private fun sincronizarConBackend(concept: String, amount: String) {
        val fechaApi      = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val montoNumerico = amount.toDoubleOrNull() ?: 0.0
        val nombreFinal   = concept.ifBlank { "Gasto Sin Nombre" }
        val imageBase64   = encodeImage(state.capturedImageUri)
        val userId        = state.userId.takeIf { it != -1 } ?: run {
            Log.w(TAG, "userId no disponible — movimiento no sincronizado")
            return
        }

        Log.d(TAG, "sincronizarConBackend: userId=$userId nombre='$nombreFinal' monto=$montoNumerico")

        val request = MovimientoRequest(
            idUsuario      = userId,
            idCategoria    = categoriaToId(state.categoria),
            nombreProducto = nombreFinal,
            monto          = montoNumerico,
            fecha          = fechaApi,
            descripcion    = "Ticket capturado desde App",
            esIngreso      = false,
            ubicacion      = "San Cristóbal de las Casas",
            imagenBase64   = imageBase64
        )

        viewModelScope.launch {
            try {
                val response = apiService.crearMovimiento(request)
                if (response.isSuccessful) {
                    val cloudUrl = response.body()?.cloudinaryUrl ?: ""
                    Log.d(TAG, "Movimiento sincronizado ✓ cloudUrl=$cloudUrl")
                    if (cloudUrl.isNotEmpty() && pendingTicketId != -1) {
                        ticketDao.updateCloudinaryUrl(pendingTicketId, cloudUrl)
                        Log.d(TAG, "cloudinaryUrl guardado en Room (ticketId=$pendingTicketId)")
                    }
                } else {
                    Log.w(TAG, "Backend rechazó HTTP ${response.code()}")
                    state = state.copy(errorMessage = "No se pudo guardar en el servidor (${response.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sin conexión: ${e.message}")
                state = state.copy(errorMessage = "Sin conexión. El ticket se guardó localmente.")
            }
        }
    }

    // Convierte el nombre de categoría al ID numérico único.
    // Cada categoría de cada rol tiene su propio ID para que la gráfica muestre el nombre correcto.
    private fun categoriaToId(nombre: String): Int = when (nombre.trim()) {
        // ── Hogar / Estudiante (IDs 1-8) ───────────────────────────────────────
        "Alimentación"          -> 1
        "Transporte"            -> 2
        "Entretenimiento"       -> 3
        "Salud"                 -> 4
        "Ropa"                  -> 5
        "Hogar"                 -> 6
        "Materiales de estudio" -> 7
        "Educación"             -> 8
        // ── Negocio (IDs 9-14) ─────────────────────────────────────────────────
        "Ventas"                -> 9
        "Compras"               -> 10
        "Gastos operacionales"  -> 11
        "Salarios"              -> 12
        "Impuestos"             -> 13
        "Servicios"             -> 14
        // ── Fallback ───────────────────────────────────────────────────────────
        else                    -> 0  // Otros / Otro
    }

    private fun encodeImage(uriString: String?): String {
        if (uriString == null) return ""
        return try {
            val uri   = Uri.parse(uriString)
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) { "" }
    }

    companion object {
        private const val TAG = "CameraViewModel"
    }
}
