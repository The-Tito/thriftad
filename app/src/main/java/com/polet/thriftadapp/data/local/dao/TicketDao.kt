package com.polet.thriftadapp.data.local.dao

import androidx.room.*
import com.polet.thriftadapp.data.local.entities.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {

    // 1. Para mostrar en el Home (Solo los activos del usuario actual)
    @Query("SELECT * FROM tickets WHERE isVisible = 1 AND userId = :userId ORDER BY id DESC")
    fun getVisibleTickets(userId: Int): Flow<List<TicketEntity>>

    // 2. Para la Galería (del usuario actual)
    @Query("SELECT * FROM tickets WHERE userId = :userId ORDER BY id DESC")
    fun getAllTickets(userId: Int): Flow<List<TicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Query("SELECT id FROM tickets ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedId(): Int?

    // 3. Esta es la que pedía el ViewModel para el borrado físico:
    @Query("DELETE FROM tickets WHERE id = :id")
    suspend fun deleteTicketById(id: Int)

    // 4. Por si quieres solo ocultarlo sin borrar la imagen
    @Query("UPDATE tickets SET isVisible = 0 WHERE id = :id")
    suspend fun archiveTicket(id: Int)

    @Query("UPDATE tickets SET cloudinaryUrl = :url WHERE id = :ticketId")
    suspend fun updateCloudinaryUrl(ticketId: Int, url: String)

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TicketEntity?

    @Update
    suspend fun update(ticket: TicketEntity)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)
}