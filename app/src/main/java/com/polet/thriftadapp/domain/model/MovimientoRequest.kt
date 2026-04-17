package com.polet.thriftadapp.domain.model

data class MovimientoRequest(
    val idUsuario: Int,
    val idCategoria: Int,
    val nombreProducto: String,
    val monto: Double,
    val fecha: String,
    val descripcion: String,
    val esIngreso: Boolean,
    val ubicacion: String,
    val imagenBase64: String
)