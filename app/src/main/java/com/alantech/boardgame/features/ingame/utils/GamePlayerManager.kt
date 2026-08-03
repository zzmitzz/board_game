package com.alantech.boardgame.features.ingame.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import okhttp3.internal.http2.Http2Reader


data class GameEngineState(
    val currentRound: Int,
    val activePlayer: GamePlayer,
    val currentCards: Set<CardDetail>,
)

interface IGamePlayManager {
    fun startGameEngine()
    fun onCardCompleted(
        timeSpent: Float, // seconds
        cardId: String
    )
    fun onCardForfeited(
        timeSpent: Float , // seconds
        cardId: String
    )
    fun resetSession()
}


class GamePlayerManager private constructor(
    val originalGamePlayers: List<GamePlayer>,
    val originalGamePacks: Set<CardDetail>
) : IGamePlayManager {

    companion object {
        fun createGamePlayerManager(
            gamePlayers: List<GamePlayer>,
            gamePacks: List<CardDetail>
        ) = GamePlayerManager(gamePlayers, gamePacks.toSet())
    }

    interface OnStateChange {
        fun onGameEnded()

        fun onRoundEnded()

        fun onNextTurn()

        fun onResetTimer()
    }

    private var listener : OnStateChange? = null

    private var mGameEngineState: MutableStateFlow<GameEngineState> = MutableStateFlow(GameEngineState(0, originalGamePlayers.first(), originalGamePacks))
    val gameEngineState: StateFlow<GameEngineState> = mGameEngineState

    private val _gamePlayersScore = mutableMapOf<GamePlayer, GamePlayerScore>()

    private val mHandler = Handler(Looper.getMainLooper())


    val gamePlayersScore: Map<GamePlayer, GamePlayerScore>
        get() = _gamePlayersScore

    fun setOnStateChangeListener(listener: OnStateChange) {
        this.listener = listener
    }

    fun removeOnStateChangeListener() {
        this.listener = null
    }

    // Internal variable to keep track of the turn order
    private var mTurnOrder = 1

    init {
        startGameEngine()
    }

    override fun startGameEngine(){
        mGameEngineState.value = GameEngineState(1, originalGamePlayers.first(), originalGamePacks.shuffled().toSet())
        if(originalGamePacks.size < originalGamePlayers.size){
            throw IllegalArgumentException("Not enough cards for all players")
        }
        if(GameSettingConfigCurrentSession.getTotalRounds() >= originalGamePacks.size){
            throw IllegalArgumentException("Not enough cards for all players")
        }
        originalGamePlayers.forEach {
            _gamePlayersScore[it] = GamePlayerScore()
        }
    }

    override fun onCardCompleted(
        timeSpent: Float,
        cardId: String
    ) {
        val activePlayer = mGameEngineState.value.activePlayer
        val gamePlayerScore = _gamePlayersScore[activePlayer] ?: return
        with(gamePlayerScore) {
            this.timeSpent += timeSpent
            this.cardIds.add(cardId)
            this.numberCardCompleted += 1
        }
        onPlayerDone()
    }

    override fun onCardForfeited(timeSpent: Float, cardId: String) {
        val activePlayer = mGameEngineState.value.activePlayer
        val gamePlayerScore = _gamePlayersScore[activePlayer] ?: return
        with(gamePlayerScore) {
            this.timeSpent += timeSpent
            this.cardIds.add(cardId)
            this.numberCardForfeited += 1
        }
        onPlayerDone()
    }
    override fun resetSession() {
        mGameEngineState.value = GameEngineState(1, originalGamePlayers.first(), originalGamePacks)
        _gamePlayersScore.clear()
    }

    private fun onPlayerDone(){
        if(mTurnOrder == originalGamePlayers.size){
            mTurnOrder = 1
            nextRound()
        }else{
            mTurnOrder++
            nextTurn()
        }
    }

    private fun nextRound(){
        if(mGameEngineState.value.currentRound == GameSettingConfigCurrentSession.getTotalRounds()){
            listener?.onGameEnded()
            return
        }
        listener?.onRoundEnded()

        val rawData = originalGamePacks.shuffled().toList()
        val alreadyAddedCard = mutableSetOf<CardDetail>()


        val newShuffledCards = mutableSetOf<CardDetail>()

        for (i in originalGamePlayers.indices){
            val cardForPlayerI = rawData.first{
                !checkIfCardIsDoneByPlayer(
                    originalGamePlayers[i],
                    it.id)
                        && !alreadyAddedCard.contains(it)
            }
            newShuffledCards.add(cardForPlayerI)
            alreadyAddedCard.add(cardForPlayerI)
        }

        Log.d("GamePlayerManager", "newShuffledCards: ${newShuffledCards.map { it.description }}")

        mHandler.postDelayed({
            mGameEngineState.value = GameEngineState(
                mGameEngineState.value.currentRound + 1,
                originalGamePlayers.first(),
                newShuffledCards.toSet()
            )
            listener?.onResetTimer()
        },1000)
    }

    private fun nextTurn(){
        val currentPlayerIndex =
            originalGamePlayers.indexOf(mGameEngineState.value.activePlayer)
        val nextPlayerIndex = ((currentPlayerIndex + 1) % originalGamePlayers.size)
        val nextPlayer = originalGamePlayers[nextPlayerIndex]
        mGameEngineState.update {
            mGameEngineState.value.copy(
                activePlayer = nextPlayer,
            )
        }
        listener?.onNextTurn()
        listener?.onResetTimer()
    }

    // Since the card index is the same as the player index
    fun getCurrentCardIndex(): Int {
        val currIndexPlayer = originalGamePlayers.indexOf(mGameEngineState.value.activePlayer)
        return currIndexPlayer
    }


    fun checkIfCardIsDoneByPlayer(
        player: GamePlayer,
        cardId: String
    ): Boolean {
        return _gamePlayersScore[player]?.cardIds?.contains(cardId) == true
    }


    

}