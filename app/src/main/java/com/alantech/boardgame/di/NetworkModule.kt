package com.alantech.boardgame.di

import com.alantech.boardgame.data.remote.BoardGameAPI
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
    fun provideBoardGameAPI(): BoardGameAPI {
        return RetrofitClient.apiService
    }
}
