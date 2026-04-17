package com.polet.thriftadapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.polet.thriftadapp.data.local.entities.HiddenTransactionEntity

@Dao
interface HiddenTransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun hide(entry: HiddenTransactionEntity)

    @Query("SELECT movimientoId FROM hidden_transactions WHERE userId = :userId")
    suspend fun getHiddenIds(userId: Int): List<String>
}
