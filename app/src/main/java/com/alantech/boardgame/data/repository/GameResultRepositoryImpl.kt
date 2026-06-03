package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.local.GameResultRepository
import com.alantech.boardgame.data.local.dao.GameResultDao
import com.alantech.boardgame.data.local.entity.GameResult
import javax.inject.Inject

class GameResultRepositoryImpl @Inject constructor(
    private val dao: GameResultDao,
) : GameResultRepository {

    override suspend fun saveGame(data: GameResult) = dao.insert(data)
}
