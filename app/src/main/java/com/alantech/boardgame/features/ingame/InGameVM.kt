package com.alantech.boardgame.features.ingame

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.R
import com.alantech.boardgame.config.GameSettingConfigCurrentSession as gameConfig
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.features.ingame.utils.GamePlayerManager

import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


sealed class UIState {
    object DataLoading : UIState()
    data class DataError(val message: String) : UIState()
    data class InGameUIState(
        val round: Int,
        val totalRound: Int,
        val currentCardIndex: Int,
        val currentPackCards: List<CardDetail>,
        val currentPlayer: GamePlayer,
        val gameTitle: String,
        val penalty: String,
    ) : UIState()
}


sealed class UIEffect {
    data object OnTimeUp : UIEffect()
    data class ShowToast(val message: String) : UIEffect()
    data object OnGameEnd : UIEffect()
    data class OnGameError (
        val message: String
    ) : UIEffect()
}

@HiltViewModel
class InGameVM @Inject constructor(
    @ApplicationContext private val context: Context,
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

    private val mGameStateListener = object : GamePlayerManager.OnStateChange {
        override fun onGameEnded() {
            onEndGame()
        }

    }

    private val scope = viewModelScope + CoroutineExceptionHandler { _, _ ->
        _uiState.value = UIState.DataError("Something went wrong")
    }


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
        // Initialize game
        Log.d("InGameVM", "packId: $packId")
        scope.launch {
            try {
                val cards =  repository.getCardsByPackId(packId)
                if (cards.isEmpty()) {
                    throw Exception("No cards found for this pack")
                }
                gamePlayerManager = GamePlayerManager.createGamePlayerManager(
                    gamePlayers = gameConfig.getPlayers(),
                    gamePacks = cards
                )
                gamePlayerManager?.setOnStateChangeListener(mGameStateListener)
                val pack = repository.getPackById(packId)

                startTimerCountDown()
                gamePlayerManager?.gameEngineState?.collectLatest { gameState ->
                    _uiState.value = UIState.InGameUIState(
                        round = gameState.currentRound,
                        totalRound = gameConfig.getTotalRounds(),
                        currentCardIndex = gamePlayerManager?.getCurrentCardIndex() ?: -1,
                        currentPlayer = gameState.activePlayer,
                        gameTitle = pack?.titleCard ?: context.getString(R.string.board_game),
                        currentPackCards = gameState.currentCards.toList(),
                        penalty = gameConfig.penaltyInput,
                    )
                    resetTimer()
                }
            } catch (e: Exception) {
                emitErrorState(e.message.toString())
            }
        }
    }

    private fun startTimerCountDown() {
        if (!gameConfig.getIsTimerOn()) {
            return
        }
        viewModelScope.launch {
            while (true) {
                if(timeLeft.value == 0){
                    delay(1000.milliseconds)
                    _uiEffect.emit(UIEffect.OnTimeUp)
                }
                if (timeLeft.value > 0) {
                    timeLeft.value -= 1
                }
                delay(1000.milliseconds)
            }
        }
    }


    private fun resetTimer() {
        timeLeft.value = 30
    }

    fun onCardComplete(
        isComplete: Boolean = true
    ) {
        if (!checkState()) return
        val state = _uiState.value as UIState.InGameUIState
        if(isComplete){
            gamePlayerManager?.onCardCompleted(
                timeSpent = 30 - timeLeft.value,
                cardId = state.currentPackCards[state.currentCardIndex].id
            )
        }else{
            gamePlayerManager?.onCardForfeited(
                timeSpent = 30 - timeLeft.value,
                cardId = state.currentPackCards[state.currentCardIndex].id
            )
        }
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

    private fun emitErrorState(
        message: String
    ){
        _uiState.value = UIState.DataError(message)
        _uiEffect.tryEmit(UIEffect.OnGameError(message))
    }

    fun resetAllData() {
        currentGameID = ""
        timeLeft.value = 30
        gamePersistenceSetting.value = PersistenceSetting()
        gamePlayerManager?.removeOnStateChangeListener()
        gamePlayerManager?.resetSession()
        _uiState.value = UIState.DataLoading
        _uiEffect.tryEmit(null)
    }

}