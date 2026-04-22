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
    private var onSyncComplete: (() -> Unit)? = null

    fun setTicketId(id: Int) { pendingTicketId = id }

    fun setSyncCallback(callback: () -> Unit) { onSyncComplete = callback }

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
            is CameraEvent.OnEsIngresoChange -> state = state.copy(esIngreso = event.esIngreso)
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
        val fechaApi      = convertirFecha(state.fecha)
        val montoNumerico = amount.toDoubleOrNull() ?: 0.0
        val nombreFinal   = concept.ifBlank { if (state.esIngreso) "Ingreso" else "Gasto Sin Nombre" }
        val imageBase64   = encodeImage(state.capturedImageUri)
        val userId        = state.userId.takeIf { it != -1 } ?: run {
            Log.w(TAG, "userId no disponible — movimiento no sincronizado")
            return
        }

        Log.d(TAG, "sincronizarConBackend: userId=$userId nombre='$nombreFinal' monto=$montoNumerico esIngreso=${state.esIngreso}")

        val request = MovimientoRequest(
            idUsuario      = userId,
            idCategoria    = categoriaToId(state.categoria),
            nombreProducto = nombreFinal,
            monto          = montoNumerico,
            fecha          = fechaApi,
            descripcion    = "Ticket capturado desde App",
            esIngreso      = state.esIngreso,
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
                    onSyncComplete?.invoke()
                    onSyncComplete = null
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

    // Convierte la fecha del formulario (dd/MM/yyyy) al formato de la API (yyyy-MM-dd).
    // Si ya viene en formato yyyy-MM-dd la devuelve sin cambios.
    // Fallback: fecha de hoy si el parseo falla.
    private fun convertirFecha(fecha: String): String {
        if (fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return fecha
        return try {
            val inputFmt  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outputFmt.format(inputFmt.parse(fecha)!!)
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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
            val uri = Uri.parse(uriString)

            // 1. Abrir el stream de la imagen
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return ""

            // 2. Redimensionar: Si la foto es gigante (ej. 4000px), la bajamos a 1024px
            // Esto mantiene la calidad suficiente para ver el texto del ticket
            val maxDimension = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height

            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val aspectRatio = width.toFloat() / height.toFloat()
                val newWidth = if (aspectRatio > 1) maxDimension else (maxDimension * aspectRatio).toInt()
                val newHeight = if (aspectRatio > 1) (maxDimension / aspectRatio).toInt() else maxDimension
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            // 3. Comprimir: Aquí es donde ocurre la magia del ahorro de peso
            val outputStream = java.io.ByteArrayOutputStream()
            // Usamos JPEG al 70% de calidad. Es el "punto dulce" entre peso y visión.
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            // 4. Convertir a Base64 sin saltos de línea (NO_WRAP)
            Base64.encodeToString(byteArray, Base64.NO_WRAP)

        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar imagen: ${e.message}")
            ""
        }
    }

    companion object {
        private const val TAG = "CameraViewModel"
    }
}
