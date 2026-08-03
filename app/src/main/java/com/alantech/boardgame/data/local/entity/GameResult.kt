package com.alantech.boardgame.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.GamePlayer
import java.util.UUID

@Entity(tableName = "game_result")
data class GameResult(
    @PrimaryKey(
        autoGenerate = true
    )
    val id: Int = 0,
    val packID: String,
    val packName: String,
    val timeStamp: Long,
    val gameScore: Map<GamePlayer, GamePlayerScore>
)