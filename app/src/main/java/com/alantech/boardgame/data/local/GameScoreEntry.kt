package com.alantech.boardgame.data.local

import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.GamePlayer
import kotlinx.serialization.Serializable

@Serializable
data class GameScoreEntry(
    val player: GamePlayer,
    val score: GamePlayerScore,
)
