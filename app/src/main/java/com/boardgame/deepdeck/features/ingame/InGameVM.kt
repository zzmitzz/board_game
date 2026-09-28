package com.boardgame.deepdeck.features.ingame

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.local.entity.GameResult
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.data.model.GameSessionCreate
import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import com.boardgame.deepdeck.data.prefs.DeviceIdRepository
import com.boardgame.deepdeck.data.repository.BoardGameRepository
import com.boardgame.deepdeck.data.repository.CardReaction
import com.boardgame.deepdeck.data.repository.SavedCardRepository
import com.boardgame.deepdeck.di.CustomPackLocally
import com.boardgame.deepdeck.features.ingame.engine.Advance
import com.boardgame.deepdeck.features.ingame.engine.AwardType
import com.boardgame.deepdeck.features.ingame.engine.Awards
import com.boardgame.deepdeck.features.ingame.engine.GameEngine
import com.boardgame.deepdeck.features.ingame.engine.GameSummary
import com.boardgame.deepdeck.features.ingame.model.GameConfig
import com.boardgame.deepdeck.features.ingame.model.GamePlayerScore
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.GamePlayer
import com.boardgame.deepdeck.utils.ListSound
import com.boardgame.deepdeck.utils.SoundUtils
import com.boardgame.deepdeck.utils.share.ShareAwardLine
import com.boardgame.deepdeck.utils.share.ShareCardData
import com.boardgame.deepdeck.utils.share.ShareImageRenderer
import com.boardgame.deepdeck.utils.share.ShareRecapData
import com.boardgame.deepdeck.work.SessionUploadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import retrofit2.HttpException

sealed interface InGameUiState {
    /** No game started yet (e.g. after "Play again"). */
    data object Idle : InGameUiState
    data object Loading : InGameUiState
    data class Error(@StringRes val message: Int) : InGameUiState

    data class Playing(
        val packTitle: String,
        val round: Int,
        val totalRounds: Int,
        val players: List<GamePlayer>,
        val playerIndex: Int,
        val card: CardDetail,
        val turnKey: Int,
        val canSkip: Boolean,
        val isSaved: Boolean = false,
        /** Pass-the-phone interstitial is showing; the card stays hidden until revealed. */
        val awaitingReveal: Boolean,
        val speedMode: Boolean,
        val penaltyText: String?,
        val paused: Boolean = false,
        val soundOn: Boolean = true,
        val hapticsOn: Boolean = true,
        val isCustomPack: Boolean,
    ) : InGameUiState {
        val player: GamePlayer get() = players[playerIndex]
        val level: CardLevel get() = CardLevel.from(card.category)
    }

    data object Finished : InGameUiState
}

sealed interface InGameUiEffect {
    data class ShowMessage(@StringRes val message: Int) : InGameUiEffect
    /** Speed mode ran out: the UI flies the card left, then calls [InGameVM.forfeit]. */
    data class TimeUp(val turnKey: Int) : InGameUiEffect
    data class RoundStarted(val round: Int) : InGameUiEffect
    data object DeckReshuffled : InGameUiEffect
    data object GameFinished : InGameUiEffect
    data class Share(val uri: Uri?, val fallbackText: String) : InGameUiEffect
}

/**
 * One game session, scoped to the game-flow nav graph (Setup → Play → End share it).
 * Replaces the old global `GameSettingConfigCurrentSession` + `GamePlayerManager` pair; the
 * config lives in [SavedStateHandle] so a recreated process can restart the game.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InGameVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteRepository: BoardGameRepository,
    @CustomPackLocally private val localRepository: BoardGameRepository,
    private val gameResultRepository: GameResultRepository,
    private val settingsRepository: AppSettingsRepository,
    private val savedCardRepository: SavedCardRepository,
    private val deviceIdRepository: DeviceIdRepository,
    private val shareImageRenderer: ShareImageRenderer,
    private val json: Json,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<InGameUiState>(InGameUiState.Idle)
    val uiState: StateFlow<InGameUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<InGameUiEffect>(extraBufferCapacity = 8)
    val uiEffect: SharedFlow<InGameUiEffect> = _uiEffect.asSharedFlow()

    /** Seconds left in speed mode (30 → 0). */
    private val _timeLeft = MutableStateFlow(GameConfig.TURN_SECONDS)
    val timeLeft: StateFlow<Float> = _timeLeft.asStateFlow()

    private val _summary = MutableStateFlow<GameSummary?>(null)
    val summary: StateFlow<GameSummary?> = _summary.asStateFlow()

    private var engine: GameEngine? = null
    private var config: GameConfig? = null
    private var packId: String = ""
    private var isCustomPack: Boolean = false
    private var cardLanguage: String = "en"
    private var startedAt: Long = 0L
    private val lovedCards = ArrayList<CardDetail>()

    private var loadJob: Job? = null
    private var tickJob: Job? = null
    private var turnSeconds = 0f
    private var timeUpSent = false
    private var foreground = true

    init {
        settingsRepository.settings
            .onEach { s -> updatePlaying { it.copy(soundOn = s.soundEnabled, hapticsOn = s.hapticsEnabled) } }
            .launchIn(viewModelScope)

        _uiState
            .map { (it as? InGameUiState.Playing)?.card?.id }
            .distinctUntilChanged()
            .flatMapLatest { id -> if (id == null) flowOf(false) else savedCardRepository.observeIsSaved(id) }
            .onEach { saved -> updatePlaying { it.copy(isSaved = saved) } }
            .launchIn(viewModelScope)
    }

    // -----------------------------------------------------------------------
    // Lifecycle of a session
    // -----------------------------------------------------------------------

    /** Called by Setup: stores the config and deals the first card. */
    fun start(packId: String, isCustomPack: Boolean, config: GameConfig) {
        savedStateHandle[KEY_PACK] = packId
        savedStateHandle[KEY_CUSTOM] = isCustomPack
        savedStateHandle[KEY_CONFIG] = json.encodeToString(GameConfig.serializer(), config)
        load(packId, isCustomPack, config)
    }

    /** Play screen entry: restarts from the saved config after process death; no-op otherwise. */
    fun ensureStarted() {
        if (_uiState.value != InGameUiState.Idle) return
        val raw = savedStateHandle.get<String>(KEY_CONFIG) ?: run {
            _uiState.value = InGameUiState.Error(R.string.game_session_lost)
            return
        }
        val restored = runCatching { json.decodeFromString(GameConfig.serializer(), raw) }.getOrNull() ?: run {
            _uiState.value = InGameUiState.Error(R.string.game_session_lost)
            return
        }
        load(savedStateHandle[KEY_PACK] ?: "", savedStateHandle[KEY_CUSTOM] ?: false, restored)
    }

    fun retry() {
        val cfg = config ?: return ensureStarted()
        load(packId, isCustomPack, cfg)
    }

    private fun load(packId: String, isCustomPack: Boolean, config: GameConfig) {
        this.packId = packId
        this.isCustomPack = isCustomPack
        this.config = config
        engine = null
        lovedCards.clear()
        _summary.value = null
        _uiState.value = InGameUiState.Loading
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                val repository = if (isCustomPack) localRepository else remoteRepository
                // One request for the whole deck, already in the card language (no per-card translate).
                cardLanguage = if (isCustomPack) "" else settingsRepository.cardLanguage()
                val titleAsync = async { runCatching { repository.getPackById(packId)?.titleCard }.getOrNull() }
                val cards = repository.getCardsByPackId(packId, cardLanguage.ifBlank { "en" })
                    .filter { it.id.isNotBlank() && it.description.isNotBlank() }
                if (cards.isEmpty()) {
                    _uiState.value = InGameUiState.Error(
                        if (isCustomPack) R.string.game_no_cards_custom else R.string.game_no_cards
                    )
                    return@launch
                }
                val title = titleAsync.await().orEmpty().ifBlank { context.getString(R.string.board_game) }
                val newEngine = GameEngine(cards, config.players, config.rounds)
                engine = newEngine
                startedAt = System.currentTimeMillis()
                val settings = settingsRepository.current()
                _uiState.value = InGameUiState.Playing(
                    packTitle = title,
                    round = 1,
                    totalRounds = config.rounds,
                    players = config.players,
                    playerIndex = 0,
                    card = newEngine.current.card,
                    turnKey = newEngine.current.turnKey,
                    canSkip = newEngine.canSkip(),
                    awaitingReveal = config.passThePhone,
                    speedMode = config.speedMode,
                    penaltyText = config.penaltyText.takeIf { config.penaltyEnabled && it.isNotBlank() },
                    soundOn = settings.soundEnabled,
                    hapticsOn = settings.hapticsEnabled,
                    isCustomPack = isCustomPack,
                )
                resetTurnTimer()
                startTicker()
                play(ListSound.START)
                if (newEngine.willRepeatCards) _uiEffect.tryEmit(InGameUiEffect.ShowMessage(R.string.game_small_deck_notice))
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                Log.w(TAG, "load failed: HTTP ${e.code()}")
                // 402: the pack became premium after it was saved (e.g. Quick Play of an old game).
                _uiState.value = InGameUiState.Error(
                    if (e.code() == 402) R.string.game_pack_premium else R.string.game_load_error
                )
            } catch (e: Exception) {
                Log.w(TAG, "load failed: ${e.message}")
                _uiState.value = InGameUiState.Error(R.string.game_load_error)
            }
        }
    }

    /** "Play again": back to Setup with the same roster; the next start() deals a new game. */
    fun resetForPlayAgain() {
        stopTicker()
        engine = null
        // The summary stays until the next start() so the End screen doesn't flash empty while leaving.
        _uiState.value = InGameUiState.Idle
    }

    /** Quit mid-game: nothing is saved or reported. */
    fun quit() {
        stopTicker()
        engine = null
        _uiState.value = InGameUiState.Idle
    }

    // -----------------------------------------------------------------------
    // Turn actions (all guarded by turnKey so a swipe + a button can't double-commit)
    // -----------------------------------------------------------------------

    fun revealTurn() {
        updatePlaying { it.copy(awaitingReveal = false) }
        resetTurnTimer()
    }

    fun complete(turnKey: Int) = answer(turnKey, completed = true)

    fun forfeit(turnKey: Int) = answer(turnKey, completed = false)

    private fun answer(turnKey: Int, completed: Boolean) {
        val state = _uiState.value as? InGameUiState.Playing ?: return
        val engine = engine ?: return
        if (state.turnKey != turnKey || state.awaitingReveal) return
        val seconds = turnSeconds
        when (val advance = if (completed) engine.complete(seconds) else engine.forfeit(seconds)) {
            is Advance.Finished -> finish()
            is Advance.NextTurn -> {
                val turn = engine.current
                val passPhone = config?.passThePhone == true
                _uiState.value = state.copy(
                    round = turn.round,
                    playerIndex = turn.playerIndex,
                    card = turn.card,
                    turnKey = turn.turnKey,
                    canSkip = engine.canSkip(),
                    awaitingReveal = passPhone,
                    isSaved = false,
                )
                resetTurnTimer()
                if (advance.newRound) {
                    play(ListSound.START)
                    _uiEffect.tryEmit(InGameUiEffect.RoundStarted(turn.round))
                } else {
                    play(ListSound.PLAY)
                }
                if (advance.reshuffled) _uiEffect.tryEmit(InGameUiEffect.DeckReshuffled)
            }
        }
    }

    /** Free skip (once per round per player): no penalty, SKIP feedback for the admin. */
    fun skip(turnKey: Int) {
        val state = _uiState.value as? InGameUiState.Playing ?: return
        val engine = engine ?: return
        if (state.turnKey != turnKey) return
        val skipped = engine.skip() ?: run {
            _uiEffect.tryEmit(InGameUiEffect.ShowMessage(R.string.skip_used))
            return
        }
        val turn = engine.current
        _uiState.value = state.copy(card = turn.card, turnKey = turn.turnKey, canSkip = engine.canSkip(), isSaved = false)
        resetTurnTimer()
        if (!isCustomPack) {
            viewModelScope.launch { savedCardRepository.sendFeedback(skipped.id, packId, CardReaction.SKIP) }
        }
    }

    fun toggleSave() {
        val state = _uiState.value as? InGameUiState.Playing ?: return
        val card = state.card
        viewModelScope.launch {
            if (state.isSaved) {
                savedCardRepository.remove(card.id)
                lovedCards.removeAll { it.id == card.id }
            } else {
                savedCardRepository.save(
                    SavedCardEntity(
                        id = card.id,
                        text = card.description,
                        back = card.backSide,
                        level = state.level.name,
                        packId = packId.takeIf { !isCustomPack },
                        packTitle = state.packTitle,
                    ),
                    sendLove = !isCustomPack
                )
                lovedCards += card
                _uiEffect.tryEmit(InGameUiEffect.ShowMessage(R.string.saved_to_library))
            }
        }
    }

    fun report() {
        val state = _uiState.value as? InGameUiState.Playing ?: return
        if (!isCustomPack) {
            viewModelScope.launch { savedCardRepository.sendFeedback(state.card.id, packId, CardReaction.REPORT) }
        }
        _uiEffect.tryEmit(InGameUiEffect.ShowMessage(R.string.report_thanks))
    }

    fun shareCurrentCard() {
        val state = _uiState.value as? InGameUiState.Playing ?: return
        shareCard(state.card, state.packTitle)
    }

    fun shareCard(card: CardDetail, packTitle: String?) {
        viewModelScope.launch {
            val uri = shareImageRenderer.renderCard(
                ShareCardData(card.description, CardLevel.from(card.category), packTitle)
            )
            _uiEffect.emit(InGameUiEffect.Share(uri, "“${card.description}”\n\n${context.getString(R.string.share_daily_footer)}"))
        }
    }

    // -----------------------------------------------------------------------
    // Pause / settings
    // -----------------------------------------------------------------------

    fun setPaused(paused: Boolean) = updatePlaying { it.copy(paused = paused) }

    /** Screen lifecycle (ON_START / ON_STOP): the timer only runs while visible. */
    fun setForeground(visible: Boolean) {
        foreground = visible
    }

    fun setSound(enabled: Boolean) = viewModelScope.launch { settingsRepository.setSound(enabled) }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch { settingsRepository.setHaptics(enabled) }

    // -----------------------------------------------------------------------
    // Timer: counts active answer time (all modes) and the 30s countdown in speed mode
    // -----------------------------------------------------------------------

    private fun resetTurnTimer() {
        turnSeconds = 0f
        timeUpSent = false
        _timeLeft.value = GameConfig.TURN_SECONDS
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(TICK_MS)
                val state = _uiState.value as? InGameUiState.Playing ?: continue
                if (!foreground || state.paused || state.awaitingReveal) continue
                turnSeconds += TICK_MS / 1000f
                if (state.speedMode) {
                    val left = (GameConfig.TURN_SECONDS - turnSeconds).coerceAtLeast(0f)
                    _timeLeft.value = left
                    if (left <= 0f && !timeUpSent) {
                        timeUpSent = true
                        _uiEffect.emit(InGameUiEffect.TimeUp(state.turnKey))
                    }
                }
            }
        }
    }

    private fun stopTicker() {
        tickJob?.cancel()
        tickJob = null
    }

    // -----------------------------------------------------------------------
    // End of game
    // -----------------------------------------------------------------------

    private fun finish() {
        val engine = engine ?: return
        val config = config ?: return
        stopTicker()
        val playing = _uiState.value as? InGameUiState.Playing
        val ranked = Awards.rank(engine.scores)
        val awards = Awards.compute(ranked, config.speedMode)
        val (cardOfNight, loved) = Awards.cardOfTheNight(engine.turnLog, lovedCards)
        val title = playing?.packTitle.orEmpty()
        val duration = ((System.currentTimeMillis() - startedAt) / 1000L).toInt().coerceAtLeast(0)
        val summary = GameSummary(
            packTitle = title,
            leaderboard = ranked,
            awards = awards,
            cardOfTheNight = cardOfNight,
            cardOfTheNightLoved = loved,
            totalCompleted = engine.scores.values.sumOf { it.numberCardCompleted },
            totalForfeited = engine.scores.values.sumOf { it.numberCardForfeited },
            durationSeconds = duration,
            rounds = config.rounds,
            speedMode = config.speedMode,
        )
        _summary.value = summary
        if (playing?.soundOn != false) runCatching { SoundUtils.play(ListSound.ACHIEVEMENT) }
        _uiState.value = InGameUiState.Finished
        persistResult(engine.scores.mapValues { it.value.copy(cardIds = it.value.cardIds.toMutableSet()) }, title)
        reportSession(summary, config)
        _uiEffect.tryEmit(InGameUiEffect.GameFinished)
    }

    private fun persistResult(scores: Map<GamePlayer, GamePlayerScore>, title: String) {
        viewModelScope.launch {
            runCatching {
                gameResultRepository.saveGame(
                    GameResult(
                        packID = packId,
                        packName = title,
                        timeStamp = System.currentTimeMillis(),
                        gameScore = scores
                    )
                )
            }.onFailure { Log.w(TAG, "save result failed: ${it.message}") }
        }
    }

    /** POST /events/sessions via WorkManager (queued while offline). */
    private fun reportSession(summary: GameSummary, config: GameConfig) {
        viewModelScope.launch {
            runCatching {
                val payload = GameSessionCreate(
                    deviceId = deviceIdRepository.get(),
                    // Custom packs live only on this device; the backend FK would reject their ids.
                    packId = packId.takeIf { !isCustomPack && it.isNotBlank() },
                    isCustomPack = isCustomPack,
                    playersCount = config.players.size,
                    rounds = config.rounds,
                    cardsCompleted = summary.totalCompleted,
                    cardsForfeited = summary.totalForfeited,
                    durationSeconds = summary.durationSeconds,
                    locale = cardLanguage.ifBlank { settingsRepository.current().uiLanguage }.take(5),
                )
                SessionUploadWorker.enqueue(context, json.encodeToString(GameSessionCreate.serializer(), payload))
            }.onFailure { Log.w(TAG, "session event not queued: ${it.message}") }
        }
    }

    /** Renders the 9:16 recap image and asks the UI to share it. */
    fun shareRecap() {
        val summary = _summary.value ?: return
        val winner = summary.winner ?: return
        viewModelScope.launch {
            val minutes = (summary.durationSeconds / 60).coerceAtLeast(1)
            val statsLine = context.getString(
                R.string.recap_stats_line,
                summary.totalCompleted + summary.totalForfeited,
                minutes
            )
            val awards = summary.awards.filter { it.type != AwardType.WINNER }
                .map { ShareAwardLine(it.type.emoji, context.getString(it.type.title), it.player.name, null) }
            val uri = shareImageRenderer.renderRecap(
                ShareRecapData(
                    packTitle = summary.packTitle,
                    winnerName = winner.player.name,
                    winnerAvatar = winner.player.avatar,
                    winnerColor = argb(winner.player),
                    winnerStat = context.getString(
                        R.string.recap_winner_stat,
                        winner.score.getScore(),
                        winner.score.numberCardCompleted
                    ),
                    awards = awards,
                    cardOfTheNight = summary.cardOfTheNight?.description,
                    statsLine = statsLine,
                )
            )
            val text = context.getString(R.string.recap_share_text, winner.player.name, summary.packTitle)
            _uiEffect.emit(InGameUiEffect.Share(uri, text))
        }
    }

    fun toggleSaveCardOfTheNight() {
        val summary = _summary.value ?: return
        val card = summary.cardOfTheNight ?: return
        viewModelScope.launch {
            savedCardRepository.save(
                SavedCardEntity(
                    id = card.id,
                    text = card.description,
                    back = card.backSide,
                    level = CardLevel.from(card.category).name,
                    packId = packId.takeIf { !isCustomPack },
                    packTitle = summary.packTitle,
                ),
                sendLove = !isCustomPack
            )
            _uiEffect.tryEmit(InGameUiEffect.ShowMessage(R.string.saved_to_library))
        }
    }

    fun isCardSaved(cardId: String) = savedCardRepository.observeIsSaved(cardId)

    // -----------------------------------------------------------------------

    private fun updatePlaying(transform: (InGameUiState.Playing) -> InGameUiState.Playing) {
        _uiState.update { if (it is InGameUiState.Playing) transform(it) else it }
    }

    private fun play(sound: ListSound) {
        val state = _uiState.value as? InGameUiState.Playing
        if (state?.soundOn != false) runCatching { SoundUtils.play(sound) }
    }

    private fun argb(player: GamePlayer): Int {
        val c = player.color
        return android.graphics.Color.argb(255, (c.red * 255).toInt(), (c.green * 255).toInt(), (c.blue * 255).toInt())
    }

    override fun onCleared() {
        stopTicker()
        super.onCleared()
    }

    private companion object {
        const val TAG = "InGameVM"
        const val TICK_MS = 100L
        const val KEY_CONFIG = "game_config"
        const val KEY_PACK = "game_pack_id"
        const val KEY_CUSTOM = "game_is_custom"
    }
}
