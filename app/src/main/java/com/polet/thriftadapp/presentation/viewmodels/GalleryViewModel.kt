package com.polet.thriftadapp.presentation.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.local.entities.TicketEntity
import com.polet.thriftadapp.data.repository.TicketRepository
import com.polet.thriftadapp.presentation.screens.gallery.GalleryState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val repository: TicketRepository,
    private val ticketDao: TicketDao,
    application: Application
) : AndroidViewModel(application) {

    var state by mutableStateOf(GalleryState())
        private set

    private var allTicketsList = emptyList<TicketEntity>()

    fun loadForUser(userId: Int) {
        viewModelScope.launch {
            repository.allTickets(userId).collectLatest { listaDeTickets ->
                allTicketsList = listaDeTickets
                filterTickets(state.searchText)
            }
        }
    }

    fun onSearchChange(query: String) {
        state = state.copy(searchText = query)
        filterTickets(query)
    }

    fun onSelectTicket(ticket: TicketEntity?) {
        state = state.copy(selectedTicket = ticket)
    }

    fun onRequestDelete(ticket: TicketEntity) {
        state = state.copy(deleteDialogTicket = ticket)
    }

    fun onDismissDeleteDialog() {
        state = state.copy(deleteDialogTicket = null)
    }

    // ---------- OPCIÓN A: Eliminar solo la foto ----------
    fun deletePhotoOnly(ticket: TicketEntity) {
        viewModelScope.launch {
            try {
                deleteLocalFile(ticket.imagePath)
                ticketDao.update(ticket.copy(imagePath = "", cloudinaryUrl = ""))
                state = state.copy(
                    deleteDialogTicket = null,
                    selectedTicket     = null,
                    message            = "Foto eliminada. El gasto mantiene su valor."
                )
            } catch (e: Exception) {
                state = state.copy(
                    deleteDialogTicket = null,
                    error              = "Error al eliminar foto: ${e.message}"
                )
            }
        }
    }

    // ---------- OPCIÓN B: Eliminar gasto completo ----------
    // El saldo NO se revierte — solo se limpia el registro local
    fun deleteCompleteGasto(ticket: TicketEntity, userId: Int) {
        viewModelScope.launch {
            try {
                if (ticket.imagePath.isNotEmpty()) deleteLocalFile(ticket.imagePath)
                ticketDao.deleteTicket(ticket)
                state = state.copy(
                    deleteDialogTicket = null,
                    selectedTicket     = null,
                    message            = "Gasto eliminado correctamente."
                )
            } catch (e: Exception) {
                state = state.copy(
                    deleteDialogTicket = null,
                    error              = "Error al eliminar gasto: ${e.message}"
                )
            }
        }
    }

    // ---------- Helpers ----------
    private fun deleteLocalFile(path: String) {
        if (path.isEmpty()) return
        try {
            val uri = Uri.parse(path)
            getApplication<Application>().contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            println("Error borrando archivo físico: ${e.message}")
        }
    }

    private fun filterTickets(query: String) {
        val filtered = if (query.isEmpty()) allTicketsList
                       else allTicketsList.filter { it.concept.contains(query, ignoreCase = true) }
        state = state.copy(tickets = filtered)
    }
}
