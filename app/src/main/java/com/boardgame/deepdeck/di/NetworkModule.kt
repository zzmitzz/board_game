package com.boardgame.deepdeck.di

import com.boardgame.deepdeck.data.remote.BoardGameEndpoint
import com.boardgame.deepdeck.data.remote.HomeDataEndpoint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBoardGameAPI(): BoardGameEndpoint {
        return RetrofitClient.apiService
    }

    @Provides
    @Singleton
    fun provideHomeDataAPI(): HomeDataEndpoint {
        return RetrofitClient.homeDataEndpoint
    }

}
