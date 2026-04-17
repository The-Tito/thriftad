package com.polet.thriftadapp.di

import com.polet.thriftadapp.data.repository.AuthRepositoryImpl
import com.polet.thriftadapp.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn //
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.polet.thriftadapp.data.repository.HomeRepositoryImpl // <--- IMPORTANTE
import com.polet.thriftadapp.domain.repository.HomeRepository // <--- IMPORTANTE
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    // --- NUEVO: VINCULAR EL REPOSITORIO DEL HOME ---
    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        homeRepositoryImpl: HomeRepositoryImpl
    ): HomeRepository

}