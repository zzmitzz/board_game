package com.alantech.boardgame.features.gamesetup

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.R
import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.utils.random
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class GameSetupUIState(
    val players: Set<GamePlayer> = emptySet(),
    val isRecordDares: Boolean = false,
    val nsfwEnable: Boolean = false,
    val speedRun: Boolean = false,
    val penalty: Boolean = false,
    val roundAmount: Int = 5
)

sealed class GameSetupUIEffect(){
    data class onToast(val message: String) : GameSetupUIEffect()
    object onGameStart: GameSetupUIEffect()
}


class GameSetupVM : ViewModel() {
    private val gameSession by lazy {
        GameSettingConfigCurrentSession
    }
    private var playerIndexID: Int = 0

    private var _uiState: MutableStateFlow<GameSetupUIState> = MutableStateFlow(
        GameSetupUIState()
    )
    val uiState: StateFlow<GameSetupUIState> = _uiState.asStateFlow()

    private var _uiEffect: MutableSharedFlow<GameSetupUIEffect> = MutableSharedFlow<GameSetupUIEffect>()
    val uiEffect: SharedFlow<GameSetupUIEffect> = _uiEffect

    fun setGamePlayer(players: List<GamePlayer>) {
        val newPlayers = players.toSet()
        _uiState.update { it.copy(players = newPlayers) }
    }

    fun addPlayer(players: Int) {
        val currentPlayer = _uiState.value.players.toMutableSet()
        repeat(players) {
            currentPlayer.add(
                GamePlayer(
                    id = playerIndexID,
                    name = "Player ${playerIndexID}",
                    color = Color.random
                )
            )
            playerIndexID++
        }

        _uiState.update { it.copy(players = currentPlayer) }
    }

    fun removePlayer(id: Int) {
        val updated = _uiState.value.players.filter { it.id != id }.toSet()
        _uiState.update { it.copy(players = updated) }
    }

    fun removeAllPlayers() {
        _uiState.update { it.copy(players = emptySet()) }
    }

    fun setRecordDares(b: Boolean) {
        _uiState.update { it.copy(isRecordDares = b) }
    }

    fun setNsfwEnable(b: Boolean) {
        _uiState.update { it.copy(nsfwEnable = b) }
    }

    fun setSpeedRun(b: Boolean) {
        _uiState.update { it.copy(speedRun = b) }
    }

    fun setPenalty(b: Boolean) {
        _uiState.update { it.copy(penalty = b) }
    }

    fun setRoundAmount(amount: Int) {
        if (amount !in 5..20) {
            return;
        }
        _uiState.update { it.copy(roundAmount = amount) }
    }

    fun saveGameConfigSession() {
        if(_uiState.value.players.size < 2){
            viewModelScope.launch {
                _uiEffect.emit(GameSetupUIEffect.onToast("Please add at least 2 players"))
            }
            return
        }
        gameSession.setupGameConfig(
            isTimerOn = _uiState.value.speedRun,
            isRecordMomentOn = _uiState.value.isRecordDares,
            isNSFWOn = _uiState.value.nsfwEnable,
            penalty = _uiState.value.penalty,
            totalRounds = _uiState.value.roundAmount
        )
        gameSession.setPlayers(
            _uiState.value.players
        )
        viewModelScope.launch {
            _uiEffect.emit(GameSetupUIEffect.onGameStart)
        }
    }
}