package com.polet.thriftadapp.di

import android.content.Context
import androidx.room.Room
import com.polet.thriftadapp.data.local.database.AppDatabase
import com.polet.thriftadapp.data.local.database.AppDatabase.Companion.MIGRATION_5_TO_6
import com.polet.thriftadapp.data.local.database.AppDatabase.Companion.MIGRATION_6_TO_7
import com.polet.thriftadapp.data.local.dao.GoalDao
import com.polet.thriftadapp.data.local.dao.HiddenTransactionDao
import com.polet.thriftadapp.data.local.dao.TicketDao
import com.polet.thriftadapp.data.location.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.polet.thriftadapp.domain.use_case.GetHomeDataUseCase
import com.polet.thriftadapp.domain.repository.HomeRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context): LocationClient =
        LocationClient(context)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "thriftad_db")
            .addMigrations(MIGRATION_5_TO_6, MIGRATION_6_TO_7)
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    @Singleton
    fun provideTicketDao(db: AppDatabase): TicketDao = db.ticketDao()

    @Provides
    @Singleton
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    @Singleton
    fun provideHiddenTransactionDao(db: AppDatabase): HiddenTransactionDao = db.hiddenTransactionDao()

    @Provides
    @Singleton
    fun provideGetHomeDataUseCase(repository: HomeRepository): GetHomeDataUseCase =
        GetHomeDataUseCase(repository)
}
