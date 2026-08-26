package com.boardgame.deepdeck.di

import android.content.Context
import com.boardgame.deepdeck.data.local.BoardGameDatabase
import com.boardgame.deepdeck.data.local.dao.GameResultDao
import com.boardgame.deepdeck.data.local.dao.LocalCardDao
import com.boardgame.deepdeck.data.local.dao.LocalPackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBoardGameDatabase(@ApplicationContext context: Context): BoardGameDatabase =
        BoardGameDatabase.create(context)

    @Provides
    @Singleton
    fun provideGameResultDao(db: BoardGameDatabase): GameResultDao = db.gameResultDao()

    @Provides
    @Singleton
    fun provideLocalPackDao(db: BoardGameDatabase): LocalPackDao = db.localPackDao()

    @Provides
    @Singleton
    fun provideLocalCardDao(db: BoardGameDatabase): LocalCardDao = db.localCardDao()
}

