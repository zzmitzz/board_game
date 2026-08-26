package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.local.dao.GameResultDao
import com.boardgame.deepdeck.data.local.entity.GameResult
import javax.inject.Inject

class GameResultRepositoryImpl @Inject constructor(
    private val dao: GameResultDao,
) : GameResultRepository {

    override suspend fun saveGame(data: GameResult) = dao.insert(data)

    override suspend fun getAll(): List<GameResult> = dao.queryAll()
}
