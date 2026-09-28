package com.boardgame.deepdeck.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.remote.BoardGameEndpoint
import com.boardgame.deepdeck.data.remote.HomeDataEndpoint
import com.boardgame.deepdeck.data.repository.BoardGameRepository
import com.boardgame.deepdeck.data.repository.BoardGameRepositoryImpl
import com.boardgame.deepdeck.data.repository.GameResultRepositoryImpl
import com.boardgame.deepdeck.data.repository.HomeDataRepository
import com.boardgame.deepdeck.data.repository.HomeDataRepositoryImpl
import com.boardgame.deepdeck.data.repository.LocalLibraryRepository
import com.boardgame.deepdeck.data.repository.LocalLibraryRepositoryImpl
import com.boardgame.deepdeck.data.repository.CustomPackLocallyRepository
import com.boardgame.deepdeck.data.local.dao.LocalCardDao
import com.boardgame.deepdeck.data.local.dao.LocalPackDao
import kotlinx.serialization.json.Json
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
        fun provideHomeDataRepository(
            api: HomeDataEndpoint,
            dataStore: DataStore<Preferences>,
            json: Json,
        ): HomeDataRepository = HomeDataRepositoryImpl(api, dataStore, json)

        @Provides
        @Singleton
        @CustomPackLocally
        fun provideCustomPackLocallyRepository(
            packDao: LocalPackDao,
            cardDao: LocalCardDao
        ): BoardGameRepository = CustomPackLocallyRepository(packDao, cardDao)
    }
}


