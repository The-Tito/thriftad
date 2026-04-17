package com.polet.thriftadapp.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationClient(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            // Solicita una ubicación fresca (activa el GPS si es necesario).
            // A diferencia de lastLocation, esto no retorna null cuando no hay caché.
            val cts = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
        } catch (e: Exception) {
            // Fallback: usar la última ubicación conocida si el GPS falla
            try { client.lastLocation.await() } catch (_: Exception) { null }
        }
    }
}