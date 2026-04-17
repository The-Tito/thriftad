package com.polet.thriftadapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val goalId: Int = 0,
    val userId: Int,
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
