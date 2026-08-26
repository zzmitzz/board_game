package com.boardgame.deepdeck.data.local

import com.boardgame.deepdeck.data.local.entity.GameResult

interface GameResultRepository {
    suspend fun saveGame(data: GameResult)
    suspend fun getAll(): List<GameResult>
}