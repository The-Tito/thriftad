package com.polet.thriftadapp.domain.model

import com.google.gson.annotations.SerializedName

data class HomeResponse(
    val userId: Int,
    val userName: String,
    val balance: Double,
    val role: String,
    val savingMeta: Double,
    val transactions: List<TransactionResponse>
)

data class TransactionResponse(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    @SerializedName(value = "isInc", alternate = ["esIngreso", "is_inc", "isIncome"])
    val isInc: Boolean,
    val ubicacion: String? = null,
    val urlImagen: String? = null
)
