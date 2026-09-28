package com.boardgame.deepdeck.features.gamesetup

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.prefs.RememberedSetup
import com.boardgame.deepdeck.data.prefs.RosterRepository
import com.boardgame.deepdeck.data.repository.BoardGameRepository
import com.boardgame.deepdeck.di.CustomPackLocally
import com.boardgame.deepdeck.features.ingame.model.GameConfig
import com.boardgame.deepdeck.ui.model.GamePlayer
import com.boardgame.deepdeck.ui.model.PlayerPalette
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameSetupUiState(
    val isLoading: Boolean = true,
    val packTitle: String? = null,
    val isCustomPack: Boolean = false,
    val quickPlay: Boolean = false,
    val players: List<GamePlayer> = emptyList(),
    val rounds: Int = GameConfig.DEFAULT_ROUNDS,
    val speedMode: Boolean = false,
    val penaltyEnabled: Boolean = false,
    val penaltyText: String = "",
    val passThePhone: Boolean = false,
) {
    val canAddPlayer: Boolean get() = players.size < GameConfig.MAX_PLAYERS
    val canStart: Boolean get() = players.size >= GameConfig.MIN_PLAYERS
}

sealed interface GameSetupUiEffect {
    data class ShowMessage(@StringRes val message: Int, val arg: Int? = null) : GameSetupUiEffect
    data class StartGame(val config: GameConfig) : GameSetupUiEffect
}

/**
 * Setup lobby, scoped to the game-flow graph so "Play again" returns to the same roster.
 * Roster + house rules are remembered across sessions ([RosterRepository]).
 */
@HiltViewModel
class GameSetupVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rosterRepository: RosterRepository,
    private val remoteRepository: BoardGameRepository,
    @CustomPackLocally private val localRepository: BoardGameRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSetupUiState())
    val uiState: StateFlow<GameSetupUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<GameSetupUiEffect>(extraBufferCapacity = 4)
    val uiEffect: SharedFlow<GameSetupUiEffect> = _uiEffect.asSharedFlow()

    private var boundPackId: String? = null
    private var nextPlayerId = 1
    /** The user touched the pass-the-phone switch → stop following the 3+ players default. */
    private var passPhoneTouched = false

    /** Idempotent: loads the remembered roster/rules and the pack title once per graph. */
    fun bind(packId: String, isCustomPack: Boolean, quickPlay: Boolean) {
        if (boundPackId == packId) return
        boundPackId = packId
        _uiState.update { it.copy(isCustomPack = isCustomPack, quickPlay = quickPlay) }
        viewModelScope.launch {
            val remembered = rosterRepository.load()
            nextPlayerId = maxOf(remembered.nextPlayerId, (remembered.players.maxOfOrNull { it.id } ?: 0) + 1)
            passPhoneTouched = remembered.passThePhone != null
            _uiState.update {
                it.copy(
                    isLoading = false,
                    players = remembered.players.take(GameConfig.MAX_PLAYERS),
                    rounds = remembered.rounds.coerceIn(GameConfig.MIN_ROUNDS, GameConfig.MAX_ROUNDS),
                    speedMode = remembered.speedMode,
                    penaltyEnabled = remembered.penaltyEnabled,
                    penaltyText = remembered.penaltyText.ifBlank { context.getString(R.string.penalty_default) },
                    passThePhone = remembered.passThePhone
                        ?: GameConfig.defaultPassThePhone(remembered.players.size),
                )
            }
        }
        viewModelScope.launch {
            val repo = if (isCustomPack) localRepository else remoteRepository
            val title = runCatching { repo.getPackById(packId)?.titleCard }.getOrNull()
            if (!title.isNullOrBlank()) _uiState.update { it.copy(packTitle = title) }
        }
    }

    // --- Players -------------------------------------------------------------

    fun addPlayer(name: String = "") {
        val state = _uiState.value
        if (!state.canAddPlayer) {
            _uiEffect.tryEmit(GameSetupUiEffect.ShowMessage(R.string.setup_max_players, GameConfig.MAX_PLAYERS))
            return
        }
        val id = nextPlayerId++
        val index = state.players.size
        val usedEmojis = state.players.map { it.emoji }.toSet()
        val emoji = PlayerPalette.emojis.firstOrNull { it !in usedEmojis } ?: PlayerPalette.emojiFor(id)
        val player = GamePlayer(
            id = id,
            color = PlayerPalette.colorFor(index),
            name = name.trim().take(MAX_NAME).ifBlank { context.getString(R.string.player_default_name, index + 1) },
            emoji = emoji,
        )
        setPlayers(state.players + player)
    }

    fun removePlayer(id: Int) = setPlayers(_uiState.value.players.filterNot { it.id == id })

    fun renamePlayer(id: Int, name: String) {
        val trimmed = name.take(MAX_NAME)
        setPlayers(_uiState.value.players.map { if (it.id == id) it.copy(name = trimmed) else it })
    }

    /** Tap on an avatar cycles its emoji. */
    fun cycleAvatar(id: Int) {
        setPlayers(_uiState.value.players.map { p ->
            if (p.id != id) return@map p
            val i = PlayerPalette.emojis.indexOf(p.emoji)
            p.copy(emoji = PlayerPalette.emojiFor(i + 1))
        })
    }

    fun clearPlayers() = setPlayers(emptyList())

    private fun setPlayers(players: List<GamePlayer>) {
        _uiState.update {
            it.copy(
                players = players,
                passThePhone = if (passPhoneTouched) it.passThePhone else GameConfig.defaultPassThePhone(players.size),
            )
        }
    }

    // --- House rules ---------------------------------------------------------

    fun setRounds(rounds: Int) =
        _uiState.update { it.copy(rounds = rounds.coerceIn(GameConfig.MIN_ROUNDS, GameConfig.MAX_ROUNDS)) }

    fun setSpeedMode(enabled: Boolean) = _uiState.update { it.copy(speedMode = enabled) }

    fun setPenaltyEnabled(enabled: Boolean) = _uiState.update { it.copy(penaltyEnabled = enabled) }

    fun setPenaltyText(text: String) = _uiState.update { it.copy(penaltyText = text.take(MAX_PENALTY)) }

    fun setPassThePhone(enabled: Boolean) {
        passPhoneTouched = true
        _uiState.update { it.copy(passThePhone = enabled) }
    }

    // --- Start ---------------------------------------------------------------

    fun onStartClick() {
        val state = _uiState.value
        if (!state.canStart) {
            _uiEffect.tryEmit(GameSetupUiEffect.ShowMessage(R.string.setup_min_players))
            return
        }
        val players = state.players.mapIndexed { i, p ->
            if (p.name.isBlank()) p.copy(name = context.getString(R.string.player_default_name, i + 1)) else p.copy(name = p.name.trim())
        }
        val penaltyOn = state.penaltyEnabled && state.penaltyText.isNotBlank()
        val config = GameConfig(
            players = players,
            rounds = state.rounds,
            speedMode = state.speedMode,
            penaltyEnabled = penaltyOn,
            penaltyText = state.penaltyText.trim(),
            passThePhone = state.passThePhone,
        )
        _uiState.update { it.copy(players = players) }
        viewModelScope.launch {
            rosterRepository.save(
                RememberedSetup(
                    players = players,
                    rounds = state.rounds,
                    speedMode = state.speedMode,
                    penaltyEnabled = state.penaltyEnabled,
                    penaltyText = state.penaltyText,
                    passThePhone = if (passPhoneTouched) state.passThePhone else null,
                    nextPlayerId = nextPlayerId,
                )
            )
        }
        _uiEffect.tryEmit(GameSetupUiEffect.StartGame(config))
    }

    private companion object {
        const val MAX_NAME = 24
        const val MAX_PENALTY = 80
    }
}
