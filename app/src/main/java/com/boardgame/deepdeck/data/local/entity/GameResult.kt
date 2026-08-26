package com.boardgame.deepdeck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.boardgame.deepdeck.features.ingame.model.GamePlayerScore
import com.boardgame.deepdeck.ui.model.GamePlayer
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