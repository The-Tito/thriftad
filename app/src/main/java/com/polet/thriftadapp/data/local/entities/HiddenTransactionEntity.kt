package com.polet.thriftadapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_transactions")
data class HiddenTransactionEntity(
    @PrimaryKey val movimientoId: String,
    val userId: Int
)
