package com.polet.thriftadapp.data.repository

import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.local.entities.TicketEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao
) {
    // 1. Obtener todos los tickets del usuario (Se actualiza solo gracias al Flow)
    fun allTickets(userId: Int): Flow<List<TicketEntity>> = ticketDao.getAllTickets(userId)

    // 2. Guardar un ticket (Localmente por ahora)
    suspend fun insertTicket(ticket: TicketEntity) {
        ticketDao.insertTicket(ticket)
        // MAÑANA: Aquí agregaremos la lógica para subir a FastAPI
    }

    // 3. Borrar un ticket
    suspend fun deleteTicket(ticket: TicketEntity) {
        ticketDao.deleteTicket(ticket)
    }
}