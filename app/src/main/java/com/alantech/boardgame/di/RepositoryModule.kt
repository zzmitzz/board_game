package com.alantech.boardgame.di

import com.alantech.boardgame.data.remote.BoardGameAPI
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.data.repository.BoardGameRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBoardGameRepository(api: BoardGameAPI): BoardGameRepository {
        return BoardGameRepositoryImpl(api)
    }
}
