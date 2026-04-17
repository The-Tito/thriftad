package com.polet.thriftadapp.presentation.screens.home

import androidx.compose.ui.graphics.Color

data class HomeState(
    val userName: String = "Polet",
    val nombreCompleto: String = "",
    val balance: Double = 0.0,
    val role: String = "estudiante",
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val isModalVisible: Boolean = false,
    val quickAmount: String = "",
    val errorMessage: String? = null,
    val isAdding: Boolean = true,
    val isEditingMeta: Boolean = false,
    val metaAhorro: Double = 0.0,
    val savingMeta: Double = 0.0,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val monthlyBudget: Double = 0.0,
    val metas: List<MetaItem> = emptyList(),
    val balanceDisponible: Double = 0.0,
    val chartData: List<PieChartData> = emptyList()
)

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val isInc: Boolean
)

data class PieChartData(
    val label: String,
    val value: Float,
    val color: Color
)

data class MetaItem(
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
)
