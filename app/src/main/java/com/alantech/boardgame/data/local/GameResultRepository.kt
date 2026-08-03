package com.alantech.boardgame.data.local

import com.alantech.boardgame.data.local.entity.GameResult

interface GameResultRepository {
    suspend fun saveGame(data: GameResult)
    suspend fun getAll(): List<GameResult>
}