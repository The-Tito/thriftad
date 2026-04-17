package com.polet.thriftadapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 0,
    val concept: String,
    val amount: Double,
    val date: String,
    val imagePath: String = "",         // URI local (content://...)
    val cloudinaryUrl: String = "",     // URL de Cloudinary (https://...)
    val location: String = "",
    val category: String = "",
    val isIncome: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isVisible: Boolean = true       // false → oculto en Home, saldo sigue restado
)
