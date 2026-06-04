package com.alantech.boardgame.features.ingame.utils

import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import javax.inject.Inject

class GamePlayerManager private constructor(
    val gamePlayers: List<GamePlayer>
) {

//    @Inject
//    val


    companion object {
        fun createGamePlayerManager(
            gamePlayers: List<GamePlayer>
        ) = GamePlayerManager(gamePlayers)
    }

    private val gamePlayersScore = mutableMapOf<GamePlayer, GamePlayerScore>()

    init {
        gamePlayersScore.clear()
        gamePlayers.forEach {
            gamePlayersScore[it] = GamePlayerScore()
        }
    }


    fun getGamePlayersScore(): Map<GamePlayer, GamePlayerScore> {
        return gamePlayersScore
    }


    fun getNextPlayer(
        currentPlayer: GamePlayer
    ): GamePlayer{
        // Find next player
        val currentPlayerIndex =
            gamePlayers.indexOf(currentPlayer)
        return gamePlayers[((currentPlayerIndex + 1) % gamePlayers.size)]
    }

    fun onUserCompleteCard(
        player: GamePlayer,
        isUserComplete: Boolean = true,
        timeSpent: Int = 30,
        cardId: String
    ) {
        val gamePlayerScore = gamePlayersScore[player]!!
        gamePlayerScore.timeSpent += timeSpent
        gamePlayerScore.cardIds.add(cardId)
        if (isUserComplete) {
            gamePlayerScore.numberCardCompleted++
        } else {
            gamePlayerScore.numberCardForfeited++
        }
    }

    fun checkIfCardIsDoneByPlayer(
        player: GamePlayer,
        cardId: String
    ): Boolean {
        return gamePlayersScore[player]?.cardIds?.contains(cardId) == true
    }

    fun clear() {
        gamePlayersScore.clear()
    }

}