package com.boardgame.deepdeck.features.ingame.engine

import androidx.annotation.StringRes
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.features.ingame.model.GamePlayerScore
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.GamePlayer

/** Awards (plan §4), in tie-break order. */
enum class AwardType(val emoji: String, @StringRes val title: Int, @StringRes val description: Int) {
    WINNER("🏆", R.string.award_winner, R.string.award_winner_desc),
    OPEN_BOOK("📖", R.string.award_open_book, R.string.award_open_book_desc),
    MYSTERY("🕵️", R.string.award_mystery, R.string.award_mystery_desc),
    SPEEDSTER("⚡", R.string.award_speedster, R.string.award_speedster_desc),
    BRAVE_HEART("🔥", R.string.award_brave_heart, R.string.award_brave_heart_desc),
}

data class PlayerAward(
    val type: AwardType,
    val player: GamePlayer,
    val score: GamePlayerScore,
)

data class LeaderboardEntry(
    val rank: Int,
    val player: GamePlayer,
    val score: GamePlayerScore,
)

/** End-of-game "Wrapped" data. */
data class GameSummary(
    val packTitle: String,
    val leaderboard: List<LeaderboardEntry>,
    val awards: List<PlayerAward>,
    val cardOfTheNight: CardDetail?,
    /** True when the card of the night was ♥-saved during the game (else: longest answer). */
    val cardOfTheNightLoved: Boolean,
    val totalCompleted: Int,
    val totalForfeited: Int,
    val durationSeconds: Int,
    val rounds: Int,
    val speedMode: Boolean,
) {
    val winner: LeaderboardEntry? get() = leaderboard.firstOrNull()
    fun awardOf(player: GamePlayer): PlayerAward? = awards.firstOrNull { it.player.id == player.id }
}

object Awards {

    /** Ranked: higher score first, ties → less total time. */
    fun rank(scores: Map<GamePlayer, GamePlayerScore>): List<LeaderboardEntry> =
        scores.entries
            .sortedWith { a, b -> a.value.compareTo(b.value) }
            .mapIndexed { i, e -> LeaderboardEntry(i + 1, e.key, e.value) }

    /**
     * Computes awards on device. Each player gets at most one award; awards are handed out in
     * [AwardType] order, so a player who qualifies for several keeps the earliest. Within one
     * award, ties go to the better-ranked player.
     */
    fun compute(ranked: List<LeaderboardEntry>, speedMode: Boolean): List<PlayerAward> {
        if (ranked.isEmpty()) return emptyList()
        val taken = HashSet<Int>()
        val result = ArrayList<PlayerAward>()

        fun give(type: AwardType, pick: (List<LeaderboardEntry>) -> LeaderboardEntry?) {
            val candidates = ranked.filter { it.player.id !in taken }
            val chosen = pick(candidates) ?: return
            taken += chosen.player.id
            result += PlayerAward(type, chosen.player, chosen.score)
        }

        // Stable max/min: `ranked` order is the tie-breaker.
        fun maxBy(list: List<LeaderboardEntry>, value: (GamePlayerScore) -> Int): LeaderboardEntry? {
            val best = list.maxOfOrNull { value(it.score) } ?: return null
            if (best <= 0) return null
            return list.first { value(it.score) == best }
        }

        give(AwardType.WINNER) { it.firstOrNull() }
        give(AwardType.OPEN_BOOK) { maxBy(it) { s -> s.numberCardCompleted } }
        give(AwardType.MYSTERY) { maxBy(it) { s -> s.numberCardForfeited } }
        if (speedMode) {
            give(AwardType.SPEEDSTER) { list ->
                val timed = list.filter { it.score.averageTime != null }
                val best = timed.minOfOrNull { it.score.averageTime!! } ?: return@give null
                timed.first { it.score.averageTime == best }
            }
        }
        give(AwardType.BRAVE_HEART) { maxBy(it) { s -> s.deepCompleted } }
        return result
    }

    /** ♥-saved card first; else the completed card that took the longest to answer. */
    fun cardOfTheNight(log: List<TurnRecord>, loved: List<CardDetail>): Pair<CardDetail?, Boolean> {
        loved.lastOrNull()?.let { return it to true }
        val longest = log.filter { it.completed }.maxByOrNull { it.seconds }
            ?: log.maxByOrNull { it.seconds }
        return longest?.card to false
    }

    fun isDeep(card: CardDetail): Boolean {
        val level = CardLevel.from(card.category)
        return level == CardLevel.DEEP || level == CardLevel.INTIMATE
    }
}
