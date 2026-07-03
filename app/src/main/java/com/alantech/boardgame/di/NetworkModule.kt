package com.alantech.boardgame.di

import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.data.remote.HomeDataEndpoint
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
