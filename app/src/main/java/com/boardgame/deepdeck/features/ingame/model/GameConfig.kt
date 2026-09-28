package com.boardgame.deepdeck.features.ingame.model

import com.boardgame.deepdeck.ui.model.GamePlayer
import kotlinx.serialization.Serializable

/**
 * Everything a game session needs, built by Setup and handed to the game-flow-scoped
 * `InGameVM` (replaces the old global `GameSettingConfigCurrentSession` singleton).
 */
@Serializable
data class GameConfig(
    val players: List<GamePlayer>,
    val rounds: Int = DEFAULT_ROUNDS,
    /** 30s answer timer per turn; time-up = forfeit. */
    val speedMode: Boolean = false,
    val penaltyEnabled: Boolean = false,
    val penaltyText: String = "",
    /** Full-screen "Pass to <name>" interstitial before each turn. */
    val passThePhone: Boolean = true,
) {
    companion object {
        const val MIN_PLAYERS = 2
        const val MAX_PLAYERS = 20
        const val MIN_ROUNDS = 1
        const val MAX_ROUNDS = 20
        const val DEFAULT_ROUNDS = 5
        const val TURN_SECONDS = 30f

        /** Pass-the-phone default: on for 3+ players. */
        fun defaultPassThePhone(playerCount: Int) = playerCount >= 3
    }
}
