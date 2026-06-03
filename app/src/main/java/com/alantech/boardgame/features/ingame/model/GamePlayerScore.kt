package com.alantech.boardgame.features.ingame.model

import com.alantech.boardgame.ui.model.GamePlayer

data class GamePlayerScore(
    var numberCardCompleted: Int = 0,
    var numberCardForfeited: Int = 0,
    var timeSpent: Long = 0,
    val cardIds: MutableSet<String> = mutableSetOf()
): Comparable<GamePlayerScore>{
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
