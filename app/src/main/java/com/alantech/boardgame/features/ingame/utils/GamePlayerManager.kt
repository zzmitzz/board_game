package com.alantech.boardgame.features.ingame.utils

import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.utils.DataStoreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


data class GameEngineState(
    val currentRound: Int,
    val activePlayer: GamePlayer,
    val currentCards: Set<CardDetail>,
)

interface IGamePlayManager {
    fun startGameEngine()
    fun onCardCompleted(
        timeSpent: Float,
        cardId: String
    )

    fun onCardForfeited(
        timeSpent: Float,
        cardId: String
    )

    fun resetSession()
}


class GamePlayerManager @Inject constructor(
    val originalGamePlayers: List<GamePlayer>,
    val originalGamePacks: Set<CardDetail>,
    private val boardGameRepository: BoardGameRepository,
    private val dataStoreUtils: DataStoreUtils,
    private val coroutineScope: CoroutineScope,
) : IGamePlayManager {

    companion object {
        private val PREF_USER_SETTING = stringPreferencesKey("pref_user_setting")

        fun createGamePlayerManager(
            gamePlayers: List<GamePlayer>,
            gamePacks: List<CardDetail>,
            boardGameRepository: BoardGameRepository,
            dataStoreUtils: DataStoreUtils,
            coroutineScope: CoroutineScope,
        ) = GamePlayerManager(
            originalGamePlayers = gamePlayers,
            originalGamePacks = gamePacks.toSet(),
            boardGameRepository = boardGameRepository,
            dataStoreUtils = dataStoreUtils,
            coroutineScope = coroutineScope,
        )
    }

    interface OnStateChange {
        fun onGameEnded()

        fun onRoundEnded()

        fun onNextTurn()

        fun onResetTimer()
    }

    private var listener: OnStateChange? = null

    private var mGameEngineState: MutableStateFlow<GameEngineState?> = MutableStateFlow(null)
    val gameEngineState: StateFlow<GameEngineState?> = mGameEngineState

    private val _gamePlayersScore = mutableMapOf<GamePlayer, GamePlayerScore>()

    val gamePlayersScore: Map<GamePlayer, GamePlayerScore>
        get() = _gamePlayersScore

    private var mTurnOrder = 1

    fun setOnStateChangeListener(listener: OnStateChange) {
        this.listener = listener
    }

    fun removeOnStateChangeListener() {
        this.listener = null
    }

    init {
        startGameEngine()
    }

    override fun startGameEngine() {
        if (originalGamePacks.size < originalGamePlayers.size) {
            throw IllegalArgumentException("Not enough cards for all players")
        }
        if (GameSettingConfigCurrentSession.getTotalRounds() >= originalGamePacks.size) {
            throw IllegalArgumentException("Not enough cards for all players")
        }
        originalGamePlayers.forEach {
            _gamePlayersScore[it] = GamePlayerScore()
        }

        coroutineScope.launch {
            val setting =
                dataStoreUtils.getSerializedData(PREF_USER_SETTING, PersistenceSetting::class.java)
                    ?: PersistenceSetting()
            val finalCards: Set<CardDetail> =
                if (setting.isAutoTranslate && setting.language.isNotBlank()) {
                    translateCard(originalGamePacks.shuffled().toSet(), setting.language).toSet()
                } else {
                    originalGamePacks.shuffled().toSet()
                }
            mGameEngineState.value = GameEngineState(
                1,
                originalGamePlayers.first(),
                finalCards
            )
        }
    }

    override fun onCardCompleted(timeSpent: Float, cardId: String) {
        val activePlayer = mGameEngineState.value?.activePlayer
        val gamePlayerScore = _gamePlayersScore[activePlayer] ?: return
        with(gamePlayerScore) {
            this.timeSpent += timeSpent
            this.cardIds.add(cardId)
            this.numberCardCompleted += 1
        }
        onPlayerDone()
    }

    override fun onCardForfeited(timeSpent: Float, cardId: String) {
        val activePlayer = mGameEngineState.value?.activePlayer
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

    private fun onPlayerDone() {
        if (mTurnOrder == originalGamePlayers.size) {
            mTurnOrder = 1
            nextRound()
        } else {
            mTurnOrder++
            nextTurn()
        }
    }

    private fun nextRound() {
        if (mGameEngineState.value?.currentRound == GameSettingConfigCurrentSession.getTotalRounds()) {
            listener?.onGameEnded()
            return
        }
        listener?.onRoundEnded()

        val rawData = originalGamePacks.shuffled().toList()
        val alreadyAddedCard = mutableSetOf<CardDetail>()
        val newShuffledCards = mutableSetOf<CardDetail>()

        for (i in originalGamePlayers.indices) {
            val cardForPlayerI = rawData.first {
                !checkIfCardIsDoneByPlayer(
                    originalGamePlayers[i],
                    it.id
                ) && !alreadyAddedCard.contains(it)
            }
            newShuffledCards.add(cardForPlayerI)
            alreadyAddedCard.add(cardForPlayerI)
        }


        coroutineScope.launch {
            val setting =
                dataStoreUtils.getSerializedData(PREF_USER_SETTING, PersistenceSetting::class.java)
                    ?: PersistenceSetting()
            Log.i("DEBUG", newShuffledCards.joinToString { it.id })
            val finalCards: Set<CardDetail> =
                if (setting.isAutoTranslate && setting.language.isNotBlank()) {
                    translateCard(newShuffledCards, setting.language).toSet()
                } else {
                    newShuffledCards.toSet()
                }
            Log.i(
                "DEBUG",
                "${finalCards.joinToString { it.id } == newShuffledCards.joinToString { it.id }}"
            )

            mGameEngineState.value = GameEngineState(
                mGameEngineState.value?.currentRound?.plus(1) ?: -1,
                originalGamePlayers.first(),
                finalCards
            )
            listener?.onResetTimer()
        }
    }

    private suspend fun translateCard(
        listCards: Set<CardDetail>,
        targetLanguage: String
    ): Set<CardDetail> {
        return withContext(Dispatchers.IO) {
            val result = listCards.map { card ->
                async(Dispatchers.IO) {
                    runCatching {
                        boardGameRepository.translateCards(
                            cardIds = listOf(card.id),
                            locale = targetLanguage
                        ).firstOrNull() ?: card
                    }.getOrDefault(card)
                }
            }.awaitAll()
            return@withContext result.toSet()
        }
    }

    private fun nextTurn() {
        val currentPlayerIndex = originalGamePlayers.indexOf(mGameEngineState.value?.activePlayer)
        val nextPlayerIndex = ((currentPlayerIndex + 1) % originalGamePlayers.size)
        val nextPlayer = originalGamePlayers[nextPlayerIndex]
        mGameEngineState.value = mGameEngineState.value?.copy(activePlayer = nextPlayer)
        listener?.onNextTurn()
        listener?.onResetTimer()
    }

    fun getCurrentCardIndex(): Int {
        return originalGamePlayers.indexOf(mGameEngineState.value?.activePlayer)
    }

    fun checkIfCardIsDoneByPlayer(player: GamePlayer, cardId: String): Boolean {
        return _gamePlayersScore[player]?.cardIds?.contains(cardId) == true
    }
}