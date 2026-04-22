package com.polet.thriftadapp.presentation.viewmodels

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.data.location.LocationClient
import com.polet.thriftadapp.domain.model.ItemLugar
import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.local.entities.TicketEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlacesViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val ticketDao: TicketDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val listaLugares = mutableStateListOf<ItemLugar>()

    private var currentUserId: Int = -1

    // Job del observer de Room — se cancela antes de iniciar uno nuevo
    private var placesObserverJob: Job? = null

    fun setUserId(userId: Int) {
        if (userId == currentUserId) return
        currentUserId = userId
        placesObserverJob?.cancel()
        placesObserverJob = viewModelScope.launch {
            ticketDao.getVisibleTickets(userId).collectLatest { tickets ->
                listaLugares.clear()
                listaLugares.addAll(tickets.map { entity ->
                    ItemLugar(
                        nombre    = entity.concept,
                        ubicacion = entity.location ?: "Ubicación no disponible",
                        monto     = "$${entity.amount}",
                        foto      = entity.imagePath
                    )
                })
            }
        }
    }

    // Limpia todo el estado al hacer logout. Debe llamarse ANTES de navegar a Login.
    fun resetState() {
        placesObserverJob?.cancel()
        placesObserverJob = null
        currentUserId = -1
        listaLugares.clear()
    }

    fun registrarNuevoGasto(
        userId: Int,
        nombre: String,
        monto: String,
        rutaFoto: String,
        esIngreso: Boolean = false,
        onGastoConfirmado: (Double, Int) -> Unit
    ) {
        if (userId == -1) {
            android.util.Log.e("PlacesViewModel", "registrarNuevoGasto: userId inválido (-1), operación cancelada")
            return
        }

        viewModelScope.launch {
            val location  = locationClient.getCurrentLocation()
            val direccion = if (location != null) {
                obtenerNombreCiudad(location.latitude, location.longitude)
            } else {
                "Ubicación no disponible"
            }

            val montoDouble = monto.toDoubleOrNull() ?: 0.0
            val conceptoFinal = nombre.ifBlank { if (esIngreso) "Ingreso" else "Gasto" }

            val ticketId = ticketDao.insertTicket(TicketEntity(
                userId    = userId,
                concept   = conceptoFinal,
                amount    = montoDouble,
                date      = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                imagePath = rutaFoto,
                location  = direccion,
                isIncome  = esIngreso
            )).toInt()

            onGastoConfirmado(montoDouble, ticketId)
        }
    }

    // Convierte coordenadas GPS a nombre de ciudad usando Geocoder (operación de red/IO)
    private suspend fun obtenerNombreCiudad(lat: Double, lon: Double): String =
        withContext(Dispatchers.IO) {
            try {
                val geocoder   = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses  = geocoder.getFromLocation(lat, lon, 1)
                val address    = addresses?.firstOrNull()
                address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                    ?: "Lat: ${"%.4f".format(lat)}, Lon: ${"%.4f".format(lon)}"
            } catch (e: Exception) {
                "Lat: ${"%.4f".format(lat)}, Lon: ${"%.4f".format(lon)}"
            }
        }
}
