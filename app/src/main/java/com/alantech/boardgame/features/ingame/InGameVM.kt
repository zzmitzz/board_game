package com.alantech.boardgame.features.ingame

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.di.RepositoryProvider
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.model.loadingCardsDetailPack
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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


class InGameVM(
    private val savedStateHandle: SavedStateHandle,
    private val repository: BoardGameRepository = RepositoryProvider.boardGameRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UIState>(UIState.DataLoading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UIEffect?>(1)
    val uiEffect: SharedFlow<UIEffect?> = _uiEffect.asSharedFlow()

    private val gameConfig by lazy {
        GameSettingConfigCurrentSession
    }

    private val scope = viewModelScope + CoroutineExceptionHandler { _, _ ->
        _uiState.value = UIState.DataError("Something went wrong")
    }

    private var cards: List<CardDetail> = emptyList()

    init {
        if (checkUpConfig()) {
            loadDataFirstStage()
        }
    }

    private fun checkUpConfig(): Boolean {
        if (gameConfig.getPlayers().isEmpty()) {
            _uiState.value = UIState.DataError("No players found")
            return false
        }
        return true
    }

    private fun loadDataFirstStage() {
        scope.launch {
            try {
                val packId = savedStateHandle.get<String>("id").orEmpty()
                cards = if (packId.isNotEmpty()) {
                    repository.getCardsByPackId(packId)
                } else {
                    loadingCardsDetailPack()
                }
                
                if (cards.isEmpty()) {
                    _uiState.value = UIState.DataError("No cards found for this pack")
                    return@launch
                }
                
                _uiState.value = UIState.InGameUIState(
                    round = 1,
                    totalRound = 10,
                    currentCard = cards.first(),
                    currentPlayer = gameConfig.getPlayers().first(),
                    gameTitle = "Game Title",
                    penalty = "Penalty"
                )
            } catch (e: Exception) {
                _uiState.value = UIState.DataError(e.message.toString())
            }
        }
    }


    fun completeCard() {
        if (!checkIfValidPlayingState()) return

        val inGameState = (_uiState.value as UIState.InGameUIState)

        val currentCardIndex = cards.indexOf(inGameState.currentCard)
        val nextCard = cards[(currentCardIndex + 1) % cards.size]
        val currentPlayerIndex =
            gameConfig.getPlayers()
                .indexOf(inGameState.currentPlayer)

        val nextPlayer =
            gameConfig.getPlayers()[((currentPlayerIndex + 1) % gameConfig.getPlayers().size)]

        _uiState.value = inGameState.copy(
            round = if(currentPlayerIndex == 1) inGameState.round + 1 else inGameState.round,
            currentCard = nextCard,
            currentPlayer = nextPlayer
        )
        if (inGameState.round == inGameState.totalRound){
            _uiEffect.tryEmit(UIEffect.OnGameEnd)
        }
    }

//    fun forfeitCard(){
//        if (!checkIfValidPlayingState()) return
//        val inGameState = (_uiState.value as UIState.InGameUIState)
//
//    }

    fun pauseGame(){

    }


    fun checkIfValidPlayingState(): Boolean {
        return _uiState.value is UIState.InGameUIState
    }
}