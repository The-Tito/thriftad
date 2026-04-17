package com.polet.thriftadapp.presentation.screens.gallery

import com.polet.thriftadapp.data.local.entities.TicketEntity

/**
 * GALLERY STATE - BLINDAJE ARQUITECTÓNICO
 * Este archivo centraliza TODO lo que la pantalla puede mostrar.
 */

data class GalleryState(
    val tickets: List<TicketEntity> = emptyList(),
    val isLoading: Boolean = false,
    val searchText: String = "",
    val selectedTicket: TicketEntity? = null,
    val deleteDialogTicket: TicketEntity? = null, // ticket esperando decisión de borrado
    val message: String? = null,
    val error: String? = null
)