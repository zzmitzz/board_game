package com.alantech.boardgame.di

import com.alantech.boardgame.data.remote.BoardGameRepository
import com.alantech.boardgame.data.repository.BoardGameRepositoryImpl

object RepositoryProvider {
    val boardGameRepository: BoardGameRepository by lazy {
        BoardGameRepositoryImpl(RetrofitClient.apiService)
    }
}
