package com.boardgame.deepdeck.features.ingame.engine

import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.features.ingame.model.GamePlayerScore
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.GamePlayer
import kotlin.random.Random

/** The card currently in front of [playerIndex]. [turnKey] changes on every new card (turn or skip). */
data class TurnState(
    val round: Int,
    val playerIndex: Int,
    val card: CardDetail,
    val turnKey: Int,
    val skipUsed: Boolean,
)

/** One answered card, kept for "Card of the night". */
data class TurnRecord(
    val card: CardDetail,
    val playerId: Int,
    val completed: Boolean,
    val seconds: Float,
)

sealed interface Advance {
    /** Next player's turn. [newRound] = a round just started; [reshuffled] = deck ran out and was reshuffled. */
    data class NextTurn(val newRound: Boolean, val reshuffled: Boolean) : Advance
    data object Finished : Advance
}

/**
 * Pure game rules (replaces `GamePlayerManager`):
 *  - one card per player per round, [totalRounds] rounds;
 *  - a player never gets a card they already answered while unseen cards remain;
 *  - when the deck runs out it is reshuffled (repeats allowed) instead of crashing;
 *  - one free skip per player per round (no penalty).
 */
class GameEngine(
    allCards: List<CardDetail>,
    val players: List<GamePlayer>,
    val totalRounds: Int,
    private val random: Random = Random.Default,
) {
    private val cards: List<CardDetail> = allCards.distinctBy { it.id }
    private val deck = ArrayDeque<CardDetail>()
    private val seen = HashMap<Int, MutableSet<String>>()
    private val skippedThisRound = HashSet<Int>()
    private val log = ArrayList<TurnRecord>()
    private var nextTurnKey = 1

    /** Scores in roster order. */
    val scores: LinkedHashMap<GamePlayer, GamePlayerScore> = LinkedHashMap<GamePlayer, GamePlayerScore>().apply {
        players.forEach { put(it, GamePlayerScore()) }
    }

    val turnLog: List<TurnRecord> get() = log

    var current: TurnState
        private set

    /** Set when the first deal already needed a reshuffle-with-repeats (fewer cards than turns). */
    val willRepeatCards: Boolean get() = cards.size < players.size * totalRounds

    init {
        require(cards.isNotEmpty()) { "A game needs at least one card" }
        require(players.size >= 2) { "A game needs at least two players" }
        refill(exclude = null)
        val first = players.first()
        current = TurnState(1, 0, draw(first, exclude = null).first, nextTurnKey++, skipUsed = false)
    }

    val currentPlayer: GamePlayer get() = players[current.playerIndex]

    fun canSkip(): Boolean = currentPlayer.id !in skippedThisRound && cards.size > 1

    fun complete(seconds: Float): Advance = record(completed = true, seconds = seconds)

    fun forfeit(seconds: Float): Advance = record(completed = false, seconds = seconds)

    /**
     * Free skip: the card goes back to the bottom of the deck and the same player draws another.
     * Returns the replaced card, or null when the skip was already used this round.
     */
    fun skip(): CardDetail? {
        if (!canSkip()) return null
        val player = currentPlayer
        val skipped = current.card
        skippedThisRound += player.id
        scores[player]?.skips = (scores[player]?.skips ?: 0) + 1
        seen.getOrPut(player.id) { HashSet() } += skipped.id
        deck.addLast(skipped)
        val (card, _) = draw(player, exclude = skipped.id)
        current = current.copy(card = card, turnKey = nextTurnKey++, skipUsed = true)
        return skipped
    }

    private fun record(completed: Boolean, seconds: Float): Advance {
        val player = currentPlayer
        val card = current.card
        scores[player]?.apply {
            timeSpent += seconds
            cardIds += card.id
            if (completed) {
                numberCardCompleted += 1
                val level = CardLevel.from(card.category)
                if (level == CardLevel.DEEP || level == CardLevel.INTIMATE) deepCompleted += 1
            } else {
                numberCardForfeited += 1
            }
        }
        seen.getOrPut(player.id) { HashSet() } += card.id
        log += TurnRecord(card, player.id, completed, seconds)

        var round = current.round
        var index = current.playerIndex + 1
        var newRound = false
        if (index >= players.size) {
            index = 0
            round += 1
            newRound = true
            skippedThisRound.clear()
        }
        if (round > totalRounds) return Advance.Finished

        val next = players[index]
        val (nextCard, reshuffled) = draw(next, exclude = card.id)
        current = TurnState(round, index, nextCard, nextTurnKey++, skipUsed = false)
        return Advance.NextTurn(newRound = newRound, reshuffled = reshuffled)
    }

    /** Returns the drawn card and whether the deck had to be reshuffled. Never throws. */
    private fun draw(player: GamePlayer, exclude: String?): Pair<CardDetail, Boolean> {
        val seenByPlayer = seen[player.id].orEmpty()
        var reshuffled = false
        fun pickUnseen() = deck.indexOfFirst { it.id != exclude && it.id !in seenByPlayer }

        if (deck.none { it.id != exclude }) {
            refill(exclude)
            reshuffled = true
        }
        var index = pickUnseen()
        if (index < 0 && !reshuffled && cards.any { it.id != exclude && it.id !in seenByPlayer }) {
            // Unseen cards exist but were dealt to others this cycle: start a new cycle.
            refill(exclude)
            reshuffled = true
            index = pickUnseen()
        }
        if (index < 0) index = deck.indexOfFirst { it.id != exclude }
        val card = if (index >= 0) deck.removeAt(index)
        else cards.firstOrNull { it.id != exclude } ?: cards.first()
        return card to reshuffled
    }

    private fun refill(exclude: String?) {
        deck.clear()
        deck.addAll(cards.filter { it.id != exclude }.shuffled(random))
    }
}
