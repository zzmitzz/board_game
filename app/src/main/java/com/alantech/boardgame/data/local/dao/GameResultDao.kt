package com.alantech.boardgame.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alantech.boardgame.data.local.entity.GameResult

@Dao
interface GameResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gameResult: GameResult)

    @Query("SELECT * FROM game_result")
    suspend fun queryAll(): List<GameResult>

    @Query("SELECT * FROM game_result WHERE id = :id")
    suspend fun getGameByID(id: Int): GameResult?

    @Query("SELECT * FROM game_result WHERE packID = :packID")
    suspend fun getGameByPackID(packID: String): List<GameResult>

    @Delete
    suspend fun deleteGameResult(gameResult: GameResult)
    
    @Update
    suspend fun updateGameResult(gameResult: GameResult)

}
