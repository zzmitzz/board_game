package com.boardgame.deepdeck.features.ingame.model

import kotlinx.serialization.Serializable

/**
 * Per-player tally. Stored inside `game_result.gameScore` (JSON), so new fields must keep
 * defaults: rows written before the upgrade decode with zeros.
 */
@Serializable
data class GamePlayerScore(
    var numberCardCompleted: Int = 0,
    var numberCardForfeited: Int = 0,
    /** Total answer time in seconds (all turns). */
    var timeSpent: Float = 0f,
    val cardIds: MutableSet<String> = mutableSetOf(),
    /** DEEP / INTIMATE cards completed (Brave Heart award). */
    var deepCompleted: Int = 0,
    /** Free skips used. */
    var skips: Int = 0,
) : Comparable<GamePlayerScore> {
    fun getScore(): Int = numberCardCompleted - numberCardForfeited

    val turns: Int get() = numberCardCompleted + numberCardForfeited

    /** Average answer time in seconds, or null before the first answer. */
    val averageTime: Float? get() = if (turns == 0) null else timeSpent / turns

    /** Higher score first; ties → less time first. */
    override fun compareTo(other: GamePlayerScore): Int {
        if (this.getScore() == other.getScore()) {
            return this.timeSpent.compareTo(other.timeSpent)
        }
        return other.getScore() - this.getScore()
    }
}
