package com.polet.thriftadapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.polet.thriftadapp.data.local.dao.GoalDao
import com.polet.thriftadapp.data.local.dao.HiddenTransactionDao
import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.local.entities.GoalEntity
import com.polet.thriftadapp.data.local.entities.HiddenTransactionEntity
import com.polet.thriftadapp.data.local.entities.TicketEntity

@Database(
    entities = [TicketEntity::class, GoalEntity::class, HiddenTransactionEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ticketDao(): TicketDao
    abstract fun goalDao(): GoalDao
    abstract fun hiddenTransactionDao(): HiddenTransactionDao

    companion object {
        val MIGRATION_5_TO_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS user_balance")
                database.execSQL("DROP TABLE IF EXISTS sync_queue")
            }
        }

        val MIGRATION_6_TO_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS hidden_transactions (
                        movimientoId TEXT NOT NULL PRIMARY KEY,
                        userId INTEGER NOT NULL
                    )"""
                )
            }
        }
    }
}
