package com.alantech.boardgame.features.ingame

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.R
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.data.local.GameResultRepository
import com.alantech.boardgame.data.local.entity.GameResult
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.features.gamesetup.GameSetupVM
import com.alantech.boardgame.features.ingame.utils.GamePlayerManager
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.utils.DataStoreUtils
import com.alantech.boardgame.utils.HapticUtils
import com.alantech.boardgame.utils.ListSound
import com.alantech.boardgame.utils.SoundUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import com.alantech.boardgame.config.GameSettingConfigCurrentSession as gameConfig


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
        val persistenceSetting: PersistenceSetting,
    ) : UIState()
}


sealed class UIEffect {
    data object OnTimeUp : UIEffect()
    data class ShowToast(val message: String) : UIEffect()
    data object OnGameEnd : UIEffect()
    data class OnGameError(
        val message: String
    ) : UIEffect()

    data object OnShuffleCardsBegin : UIEffect()
    data object OnShuffleCardsEnd : UIEffect()
}

@HiltViewModel
open class InGameVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: BoardGameRepository,
    private val gameResultRepository: GameResultRepository,
    private val dataStoreUtils: DataStoreUtils,
) : ViewModel() {

    companion object {
        val PREF_USER_SETTING = stringPreferencesKey("pref_user_setting")
    }

    var currentGameID: String = ""
    private var currentPackId: String = ""
    private var currentPackName: String = ""
    private val _uiState = MutableStateFlow<UIState>(UIState.DataLoading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<UIEffect?>(0)
    val uiEffect: SharedFlow<UIEffect?> = _uiEffect.asSharedFlow()

    var gamePlayerManager: GamePlayerManager? = null
    val timeLeft: MutableStateFlow<Float> = MutableStateFlow(30f)

    private var sessionScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())

    private val currentSetting: PersistenceSetting?
        get() = (_uiState.value as? UIState.InGameUIState)?.persistenceSetting

    var triggerGameFlow : StateFlow<Any>? = null

    private val mGameStateListener = object : GamePlayerManager.OnStateChange {
        override fun onGameEnded() {
            currentSetting?.let {
                if (it.isHapticOn) HapticUtils.endGame(context)
                if (it.isSoundOn) SoundUtils.play(ListSound.ACHIEVEMENT)
            }
            onEndGame()
        }

        override fun onRoundEnded() {
            resetTimer()
            currentSetting?.let {
                if (it.isHapticOn) HapticUtils.nextRound(context)
                if (it.isSoundOn) SoundUtils.play(ListSound.START)
            }
            viewModelScope.launch {
                _uiEffect.emit(UIEffect.OnShuffleCardsBegin)
            }
        }

        override fun onNextTurn() {
            currentSetting?.let {
                if (it.isHapticOn) HapticUtils.short(context)
                if (it.isSoundOn) SoundUtils.play(ListSound.PLAY)
            }
        }

        override fun onResetTimer() {
            viewModelScope.launch {
                _uiEffect.emit(UIEffect.OnShuffleCardsEnd)
            }
            resetTimer()
        }
    }


    private fun checkState(): Boolean = _uiState.value is UIState.InGameUIState

    private fun checkUpConfig(): Boolean {
        Log.d("InGameVM", "checkUpConfig: $gameConfig")
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
        resetAllData()
        if (!checkUpConfig()) return
        Log.d("InGameVM", "packId: $packId")
        viewModelScope.launch {
            try {
                val cards = repository.getCardsByPackId(packId, "en")
                if (cards.isEmpty()) {
                    throw Exception("No cards found for this pack")
                }
                gamePlayerManager = GamePlayerManager.createGamePlayerManager(
                    gamePlayers = gameConfig.getPlayers(),
                    gamePacks = cards,
                    boardGameRepository = repository,
                    dataStoreUtils = dataStoreUtils,
                    coroutineScope = sessionScope,
                )
                gamePlayerManager?.setOnStateChangeListener(mGameStateListener)
                val pack = repository.getPackById(packId)
                currentPackId = packId
                currentPackName = pack?.titleCard ?: ""
                resetTimer()
                startTimerCountDown()
                val settingFow = getSettingAsFlow()
                gamePlayerManager?.gameEngineState?.let { gameStateFlow ->
                    triggerGameFlow = combine(
                        settingFow, gameStateFlow
                    ) { setting, gameState ->
                        if(gameState != null){
                            _uiState.value = UIState.InGameUIState(
                                round = gameState.currentRound,
                                totalRound = gameConfig.getTotalRounds(),
                                currentCardIndex = gamePlayerManager?.getCurrentCardIndex() ?: -1,
                                currentPlayer = gameState.activePlayer,
                                gameTitle = pack?.titleCard ?: context.getString(R.string.board_game),
                                currentPackCards = gameState.currentCards.toList(),
                                penalty = gameConfig.penaltyInput,
                                persistenceSetting = setting ?: PersistenceSetting(),
                            )
                        }
                    }
                        .stateIn(
                            scope = sessionScope,
                            started = SharingStarted.WhileSubscribed(5000),
                            UIState.DataLoading
                        )
                }
            } catch (e: Exception) {
                emitErrorState(e.message.toString())
            }
        }
    }

    fun saveSetting(
        isHapticEnable: Boolean,
        isSoundEnable: Boolean
    ) {
        viewModelScope.launch {
            val setting = dataStoreUtils.getSerializedData(
                GameSetupVM.PREF_USER_SETTING,
                PersistenceSetting::class.java
            )
                ?: PersistenceSetting()
            dataStoreUtils.setSerializedData(
                GameSetupVM.PREF_USER_SETTING, setting.copy(
                    isHapticOn = isHapticEnable,
                    isSoundOn = isSoundEnable
                )
            )
        }
    }

    private fun getSettingAsFlow(): Flow<PersistenceSetting?> =
        dataStoreUtils.getFlow(PREF_USER_SETTING, PersistenceSetting::class.java)


    private var countingTimerJob: Job? = null
    private fun startTimerCountDown() {
        if (!gameConfig.getIsTimerOn()) {
            return
        }
        sessionScope.launch {
            if(countingTimerJob?.isActive == true){
                countingTimerJob?.cancel()
            }
            countingTimerJob = launch {
                while (true) {
                    if (timeLeft.value <= 0f) {
                        delay(1000.milliseconds)
                        _uiEffect.emit(UIEffect.OnTimeUp)
                    }
                    if (timeLeft.value > 0) {
                        timeLeft.value -= 0.1f
                    }
                    delay(100.milliseconds)
                }
            }
        }
    }

    fun resumeTimer(){
        startTimerCountDown()
    }
    fun pauseTimer(){
        countingTimerJob?.cancel()
    }

    private fun resetTimer() {
        timeLeft.value = 30f
    }

    fun onCardComplete(
        isComplete: Boolean = true
    ) {
        if (!checkState()) return
        val state = _uiState.value as UIState.InGameUIState
        if (isComplete) {
            gamePlayerManager?.onCardCompleted(
                timeSpent = 30f - timeLeft.value,
                cardId = state.currentPackCards[state.currentCardIndex].id
            )
        } else {
            gamePlayerManager?.onCardForfeited(
                timeSpent = 30 - timeLeft.value,
                cardId = state.currentPackCards[state.currentCardIndex].id
            )
        }
    }


    fun onEndGame(
        isUserExitGame: Boolean = false
    ) {
        if (!isUserExitGame) {
            saveGameResult()
        }
        viewModelScope.launch {
            _uiEffect.emit(UIEffect.OnGameEnd)
        }
    }

    private fun saveGameResult() {
        val scores = gamePlayerManager?.gamePlayersScore ?: return
        viewModelScope.launch {
            gameResultRepository.saveGame(
                GameResult(
                    packID = currentPackId,
                    packName = currentPackName,
                    timeStamp = System.currentTimeMillis(),
                    gameScore = scores.toMap()
                )
            )
        }
    }

    private fun emitErrorState(message: String) {
        _uiState.value = UIState.DataError(message)
        viewModelScope.launch {
            _uiEffect.emit(UIEffect.OnGameError(message))
        }
    }

    fun resetAllData() {
        currentGameID = ""
        currentPackId = ""
        currentPackName = ""
        timeLeft.value = 30f
        _uiState.value = UIState.DataLoading
        _uiEffect.tryEmit(null)
        sessionScope.cancel()
        countingTimerJob?.cancel()
        sessionScope = CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())
        gamePlayerManager?.removeOnStateChangeListener()
        gamePlayerManager?.resetSession()
    }

}