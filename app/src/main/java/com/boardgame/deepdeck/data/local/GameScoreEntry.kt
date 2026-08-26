package com.boardgame.deepdeck.data.local

import com.boardgame.deepdeck.features.ingame.model.GamePlayerScore
import com.boardgame.deepdeck.ui.model.GamePlayer
import kotlinx.serialization.Serializable

@Serializable
data class GameScoreEntry(
    val player: GamePlayer,
    val score: GamePlayerScore,
)
