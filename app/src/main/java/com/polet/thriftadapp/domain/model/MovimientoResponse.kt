package com.polet.thriftadapp.domain.model

import com.google.gson.annotations.SerializedName

data class MovimientoResponse(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    @SerializedName(value = "isInc", alternate = ["esIngreso", "is_inc", "isIncome"])
    val isInc: Boolean,
    val ubicacion: String?,
    val urlImagen: String?
)
