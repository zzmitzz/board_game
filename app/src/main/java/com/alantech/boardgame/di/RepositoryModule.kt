package com.alantech.boardgame.di

import com.alantech.boardgame.data.local.GameResultRepository
import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.data.remote.HomeDataEndpoint
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.data.repository.BoardGameRepositoryImpl
import com.alantech.boardgame.data.repository.GameResultRepositoryImpl
import com.alantech.boardgame.data.repository.HomeDataRepository
import com.alantech.boardgame.data.repository.HomeDataRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGameResultRepository(impl: GameResultRepositoryImpl): GameResultRepository

    companion object {
        @Provides
        @Singleton
        fun provideBoardGameRepository(api: BoardGameEndpoint): BoardGameRepository =
            BoardGameRepositoryImpl(api)

        @Provides
        @Singleton
        fun provideHomeDataRepository(api: HomeDataEndpoint): HomeDataRepository =
            HomeDataRepositoryImpl(api)
    }
}

