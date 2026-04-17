package com.polet.thriftadapp.domain.model

data class CrearMovimientoResponse(
    val movimientoId: String,
    val idUsuario: Int,
    val nombreProducto: String,
    val monto: Double,
    val fecha: String,
    val cloudinaryUrl: String
)
