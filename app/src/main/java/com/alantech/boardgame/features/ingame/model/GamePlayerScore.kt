package com.alantech.boardgame.features.ingame.model

import kotlinx.serialization.Serializable

@Serializable
data class GamePlayerScore(
    var numberCardCompleted: Int = 0,
    var numberCardForfeited: Int = 0,
    var timeSpent: Float = 0f,
    val cardIds: MutableSet<String> = mutableSetOf()
) : Comparable<GamePlayerScore> {
    fun getScore(): Int {
        return numberCardCompleted - numberCardForfeited
    }

    override fun compareTo(other: GamePlayerScore): Int {
        if (this.getScore() == other.getScore()) {
            return this.timeSpent.compareTo(other.timeSpent)
        }
        return other.getScore() - this.getScore()
    }
}
