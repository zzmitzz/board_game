package com.alantech.boardgame.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.alantech.boardgame.data.local.GameResultRepository
import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.data.remote.HomeDataEndpoint
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.data.repository.BoardGameRepositoryImpl
import com.alantech.boardgame.data.repository.GameResultRepositoryImpl
import com.alantech.boardgame.data.repository.HomeDataRepository
import com.alantech.boardgame.data.repository.HomeDataRepositoryImpl
import com.alantech.boardgame.data.repository.LocalLibraryRepository
import com.alantech.boardgame.data.repository.LocalLibraryRepositoryImpl
import com.alantech.boardgame.data.repository.CustomPackLocallyRepository
import com.alantech.boardgame.data.local.dao.LocalCardDao
import com.alantech.boardgame.data.local.dao.LocalPackDao
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

    @Binds
    @Singleton
    abstract fun bindLocalLibraryRepository(impl: LocalLibraryRepositoryImpl): LocalLibraryRepository

    companion object {
        @Provides
        @Singleton
        fun provideBoardGameRepository(
            api: BoardGameEndpoint,
            dataStore: DataStore<Preferences>
        ): BoardGameRepository = BoardGameRepositoryImpl(api, dataStore)

        @Provides
        @Singleton
        fun provideHomeDataRepository(api: HomeDataEndpoint): HomeDataRepository =
            HomeDataRepositoryImpl(api)

        @Provides
        @Singleton
        @CustomPackLocally
        fun provideCustomPackLocallyRepository(
            packDao: LocalPackDao,
            cardDao: LocalCardDao
        ): BoardGameRepository = CustomPackLocallyRepository(packDao, cardDao)
    }
}


