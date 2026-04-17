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

    fun setUserId(userId: Int) {
        if (userId == currentUserId) return
        currentUserId = userId
        viewModelScope.launch {
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

    fun registrarNuevoGasto(userId: Int, nombre: String, monto: String, rutaFoto: String, onGastoConfirmado: (Double, Int) -> Unit) {
        viewModelScope.launch {
            val location  = locationClient.getCurrentLocation()
            val direccion = if (location != null) {
                obtenerNombreCiudad(location.latitude, location.longitude)
            } else {
                "Ubicación no disponible"
            }

            val montoDouble = monto.toDoubleOrNull() ?: 0.0

            val ticketId = ticketDao.insertTicket(TicketEntity(
                userId    = userId,
                concept   = nombre.ifBlank { "Gasto" },
                amount    = montoDouble,
                date      = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                imagePath = rutaFoto,
                location  = direccion
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
                address?.locality               // nombre de ciudad (ej. "San Cristóbal de las Casas")
                    ?: address?.subAdminArea    // municipio si no hay ciudad
                    ?: address?.adminArea       // estado si no hay municipio
                    ?: "Lat: ${"%.4f".format(lat)}, Lon: ${"%.4f".format(lon)}"
            } catch (e: Exception) {
                "Lat: ${"%.4f".format(lat)}, Lon: ${"%.4f".format(lon)}"
            }
        }
}
