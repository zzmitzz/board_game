package com.alantech.boardgame.features.ingame

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.config.GameSettingConfigCurrentSession as gameConfig
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.features.ingame.utils.GamePlayerManager

import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.model.loadingCardsDetailPack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject
import kotlin.math.round


sealed class UIState {
    object DataLoading : UIState()
    data class DataError(val message: String) : UIState()
    data class InGameUIState(
        val round: Int,
        val totalRound: Int,
        val currentCard: CardDetail,
        val currentPlayer: GamePlayer,
        val gameTitle: String,
        val penalty: String,
    ) : UIState()
}


sealed class UIEffect {
    data class ShowToast(val message: String) : UIEffect()
    data object OnGameEnd : UIEffect()
}

@HiltViewModel
class InGameVM @Inject constructor(
    private val repository: BoardGameRepository
) : ViewModel() {

    var currentGameID: String = ""
    private val _uiState = MutableStateFlow<UIState>(UIState.DataLoading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<UIEffect?>(1)
    val uiEffect: SharedFlow<UIEffect?> = _uiEffect.asSharedFlow()

    var gamePersistenceSetting = MutableStateFlow<PersistenceSetting>(PersistenceSetting())

    var gamePlayerManager: GamePlayerManager? = null
    val timeLeft: MutableStateFlow<Int> = MutableStateFlow(30)


    // This variable is used to track number of player that have completed a card in the current round
    private var playerCountTrack: Int = 0
    private val scope = viewModelScope + CoroutineExceptionHandler { _, _ ->
        _uiState.value = UIState.DataError("Something went wrong")
    }

    private var cards: List<CardDetail> = emptyList()


    private fun checkState(): Boolean = _uiState.value is UIState.InGameUIState

    private fun checkUpConfig(): Boolean {
        if (gameConfig.getPlayers().size < 2) {
            emitErrorState("No players found")
            return false
        }
        if (gameConfig.getPenalty() && gameConfig.penaltyInput.isEmpty()) {
            emitErrorState("No penalty content found")
            return false
        }
        return true
    }

    fun initGame(packId: String) {
        // Check if all configs are valid
        if (!checkUpConfig()) return
        gamePlayerManager = GamePlayerManager.createGamePlayerManager(gameConfig.getPlayers())
        // Initialize game
        Log.d("InGameVM", "packId: $packId")
        scope.launch {
            try {
                cards = getCardsByPackId(packId)
                if (cards.isEmpty()) {
                    emitErrorState("No cards found for this pack")
                    return@launch
                }

                _uiState.value = UIState.InGameUIState(
                    round = 1,
                    totalRound = gameConfig.getTotalRounds(),
                    currentCard = cards.first(),
                    currentPlayer = gameConfig.getPlayers().first(),
                    gameTitle = "Board Game",
                    penalty = gameConfig.penaltyInput,
                )
                startTimerCountDown()
            } catch (e: Exception) {
                _uiState.value = UIState.DataError(e.message.toString())
            }
        }
    }

    private suspend fun getCardsByPackId(packId: String) = if (packId.isNotEmpty()) {
        repository.getCardsByPackId(packId)
    } else {
        loadingCardsDetailPack()
    }.shuffled()

    private fun startTimerCountDown() {
        if (!gameConfig.getIsTimerOn()) {
            return
        }
        viewModelScope.launch {
            while (true) {
                if (timeLeft.value > 0) {
                    timeLeft.value -= 1
                }
                delay(1000)
            }
        }
    }


    private fun resetTimer() {
        timeLeft.value = 30
    }

    fun onUserDoneCard(
        isComplete: Boolean = true
    ) {
        if (!checkState()) return
        val state = _uiState.value as UIState.InGameUIState
        gamePlayerManager?.onUserCompleteCard(
            player = state.currentPlayer,
            isUserComplete = isComplete,
            timeSpent = 30 - timeLeft.value,
            cardId = state.currentCard.id
        )
        nextCardAction()
    }

    fun onSaveSetting(
        setting: PersistenceSetting
    ) {
        gamePersistenceSetting.value = setting
    }

    fun onEndGame() {
        _uiEffect.tryEmit(
            UIEffect.OnGameEnd
        )
    }


    private fun nextCardAction() {
        if (!checkState()) return
        val inGameState = (_uiState.value as UIState.InGameUIState)

        cards = cards.shuffled()

        // 1. Find next player
        val nextPlayer = gamePlayerManager?.getNextPlayer(
            currentPlayer = inGameState.currentPlayer
        )

        // 2. Update player count track, if all players have completed a card in the round, reset the count and + 1-th round
        playerCountTrack += 1

        if (playerCountTrack == gameConfig.getPlayers().size) {
            playerCountTrack = 0
            if(inGameState.round + 1 > inGameState.totalRound){
                onEndGame()
                return
            }
        }
        // 3. Find next card that is not done by the player
        val nextCard = cards.find {
            !(gamePlayerManager!!.checkIfCardIsDoneByPlayer(
                player = nextPlayer!!,
                cardId = it.id
            ))
        }

        if(nextCard == null){
            emitErrorState("No card found")
            return
        }

        _uiState.value = inGameState.copy(
            round = if (playerCountTrack == 0) inGameState.round + 1 else inGameState.round,
            currentCard = nextCard,
            currentPlayer = nextPlayer!!
        )
        resetTimer()
    }

    private fun emitErrorState(
        message: String
    ){
        _uiState.value = UIState.DataError(message)
    }

    fun resetAllData() {
        currentGameID = ""
        cards = emptyList()
        playerCountTrack = 0
        timeLeft.value = 30
        gamePersistenceSetting.value = PersistenceSetting()
        gamePlayerManager?.clear()
        _uiState.value = UIState.DataLoading
        _uiEffect.tryEmit(null)
    }

}