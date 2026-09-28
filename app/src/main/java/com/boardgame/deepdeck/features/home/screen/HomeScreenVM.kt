package com.boardgame.deepdeck.features.home.screen

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.data.model.DailyCardResponse
import com.boardgame.deepdeck.data.model.HomeSectionDto
import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import com.boardgame.deepdeck.data.prefs.DailyStreakRepository
import com.boardgame.deepdeck.data.prefs.RosterRepository
import com.boardgame.deepdeck.data.prefs.StreakState
import com.boardgame.deepdeck.data.repository.HomeDataRepository
import com.boardgame.deepdeck.data.repository.HomeFeed
import com.boardgame.deepdeck.data.repository.LocalLibraryRepository
import com.boardgame.deepdeck.data.repository.SavedCardRepository
import com.boardgame.deepdeck.features.home.model.VibeUi
import com.boardgame.deepdeck.features.home.model.toVibeUi
import com.boardgame.deepdeck.ui.model.GamePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Tonight's Card. */
data class DailyCardUi(
    val cardId: String,
    val text: String,
    val backSide: String?,
    val level: CardLevel,
    val packId: String?,
    val packTitle: String?,
    val date: String?,
)

/** "Play again with Anna, Minh +2 · Late Night". */
data class QuickPlayUi(
    val packId: String,
    val packName: String,
    val players: List<GamePlayer>,
    val isCustomPack: Boolean,
)

data class HomeScreenUIState(
    /** First load with nothing to show yet → skeleton. */
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** Set only when there is no content at all → inline error + retry. */
    val errorMessage: String? = null,
    /** Content comes from the offline cache. */
    val isStale: Boolean = false,
    val dailyCard: DailyCardUi? = null,
    val dailyRevealed: Boolean = false,
    val dailySaved: Boolean = false,
    val streak: StreakState = StreakState(),
    val vibes: List<VibeUi> = emptyList(),
    val quickPlay: QuickPlayUi? = null,
    val sections: List<HomeSectionDto> = emptyList(),
) {
    val hasContent: Boolean get() = dailyCard != null || vibes.isNotEmpty() || sections.isNotEmpty()
}

sealed class HomeScreenUIEffect {
    data class ShowMessage(@StringRes val message: Int) : HomeScreenUIEffect()

    /** Streak increased from a reveal (UI: haptic + particles). */
    data class DailyRevealed(val streak: Int) : HomeScreenUIEffect()

    /** Share the rendered 9:16 card image ([uri] null → text fallback). */
    data class ShareDaily(val uri: android.net.Uri?, val text: String) : HomeScreenUIEffect()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeScreenVM @Inject constructor(
    private val homeDataRepository: HomeDataRepository,
    private val settingsRepository: AppSettingsRepository,
    private val streakRepository: DailyStreakRepository,
    private val savedCardRepository: SavedCardRepository,
    private val gameResultRepository: GameResultRepository,
    private val localLibraryRepository: LocalLibraryRepository,
    private val rosterRepository: RosterRepository,
    private val shareImageRenderer: com.boardgame.deepdeck.utils.share.ShareImageRenderer,
) : ViewModel() {

    /** Vibe picked during onboarding; its tile is shown first. */
    private var preferredVibe: String? = null

    private val _uiState = MutableStateFlow(HomeScreenUIState())
    val uiState: StateFlow<HomeScreenUIState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeScreenUIEffect>(extraBufferCapacity = 4)
    val uiEffect: SharedFlow<HomeScreenUIEffect> = _uiEffect.asSharedFlow()

    private var loadJob: Job? = null

    init {
        streakRepository.streak
            .onEach { streak ->
                _uiState.update {
                    it.copy(streak = streak, dailyRevealed = it.dailyRevealed || streak.revealedToday)
                }
            }
            .launchIn(viewModelScope)

        _uiState
            .map { it.dailyCard?.cardId }
            .distinctUntilChanged()
            .flatMapLatest { id -> if (id == null) flowOf(false) else savedCardRepository.observeIsSaved(id) }
            .onEach { saved -> _uiState.update { it.copy(dailySaved = saved) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            homeDataRepository.getCachedHomeFeed()?.let { cached ->
                // Only use cache if the network hasn't answered first.
                if (_uiState.value.isLoading) applyFeed(cached)
            }
        }
        settingsRepository.preferredVibe
            .onEach { key ->
                preferredVibe = key
                _uiState.update { it.copy(vibes = it.vibes.sortedByPreference()) }
            }
            .launchIn(viewModelScope)
        // Language changes recreate the Activity but keep this VM: refetch server content in the new language.
        settingsRepository.settings
            .map { it.uiLanguage }
            .distinctUntilChanged()
            .drop(1)
            .onEach { load(isRefresh = false) }
            .launchIn(viewModelScope)
        load(isRefresh = false)
        refreshQuickPlay()
    }

    private fun List<VibeUi>.sortedByPreference(): List<VibeUi> {
        val key = preferredVibe ?: return this
        return sortedByDescending { it.iconKey.equals(key, ignoreCase = true) }
    }

    /** Pull-to-refresh / Retry: real network round-trip, no artificial delay. */
    fun refresh() = load(isRefresh = true)

    /** Called when Home resumes (e.g. after a game) to update the Quick Play strip. */
    fun onResume() = refreshQuickPlay()

    private fun load(isRefresh: Boolean) {
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                isRefreshing = isRefresh,
                isLoading = !it.hasContent,
                errorMessage = null
            )
        }
        loadJob = viewModelScope.launch {
            val lang = settingsRepository.contentLanguage()
            homeDataRepository.getHomeFeed(lang)
                .onSuccess { feed -> applyFeed(feed) }
                .onFailure { error ->
                    // A fast network failure can beat the cache read in init; fall back to the cache here too.
                    if (!_uiState.value.hasContent) homeDataRepository.getCachedHomeFeed()?.let(::applyFeed)
                    val hasContent = _uiState.value.hasContent
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isStale = hasContent,
                            errorMessage = if (hasContent) null else (error.message ?: "error")
                        )
                    }
                    if (hasContent && isRefresh) {
                        _uiEffect.tryEmit(HomeScreenUIEffect.ShowMessage(R.string.refresh_failed))
                    }
                }
        }
    }

    private fun applyFeed(feed: HomeFeed) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = if (feed.fromCache) it.isRefreshing else false,
                errorMessage = null,
                isStale = feed.fromCache,
                dailyCard = feed.dailyCard?.toUi() ?: it.dailyCard.takeIf { _ -> feed.fromCache },
                vibes = feed.vibes.mapNotNull { v -> v.toVibeUi() }.sortedByPreference(),
                sections = feed.sections.filter { s -> s.packs.isNotEmpty() && s.id != null },
            )
        }
    }

    private fun refreshQuickPlay() {
        viewModelScope.launch {
            val last = runCatching { gameResultRepository.getAll() }.getOrNull()
                ?.maxByOrNull { it.timeStamp }
            val quickPlay = last?.let { result ->
                val players = result.gameScore.keys.toList()
                if (players.size < 2 || result.packID.isBlank()) return@let null
                val isCustom = runCatching {
                    localLibraryRepository.getAllPacks().first().any { it.id == result.packID }
                }.getOrDefault(false)
                QuickPlayUi(
                    packId = result.packID,
                    packName = result.packName,
                    players = players,
                    isCustomPack = isCustom
                )
            }
            _uiState.update { it.copy(quickPlay = quickPlay) }
        }
    }

    /**
     * Quick Play: makes sure the remembered roster is the last game's roster (history rows
     * from before the roster store existed), then hands the target back to navigate.
     */
    fun prepareQuickPlay(onReady: (QuickPlayUi) -> Unit) {
        val quickPlay = _uiState.value.quickPlay ?: return
        viewModelScope.launch {
            val remembered = rosterRepository.load()
            val sameRoster = remembered.players.map { it.id } == quickPlay.players.map { it.id }
            if (!sameRoster) {
                rosterRepository.save(
                    remembered.copy(
                        players = quickPlay.players,
                        nextPlayerId = maxOf(remembered.nextPlayerId, (quickPlay.players.maxOfOrNull { it.id } ?: 0) + 1)
                    )
                )
            }
            onReady(quickPlay)
        }
    }

    fun revealDaily() {
        if (_uiState.value.dailyRevealed) return
        _uiState.update { it.copy(dailyRevealed = true) }
        viewModelScope.launch {
            val before = _uiState.value.streak.current
            val streak = streakRepository.registerReveal()
            if (streak.current != before) {
                _uiEffect.tryEmit(HomeScreenUIEffect.DailyRevealed(streak.current))
            }
        }
    }

    /** Renders Tonight's Card as a branded story image, then asks the UI to share it. */
    fun shareDaily(overline: String, fallbackText: String) {
        val card = _uiState.value.dailyCard ?: return
        viewModelScope.launch {
            val uri = shareImageRenderer.renderCard(
                com.boardgame.deepdeck.utils.share.ShareCardData(
                    text = card.text,
                    level = card.level,
                    packTitle = card.packTitle,
                    overline = overline
                )
            )
            _uiEffect.tryEmit(HomeScreenUIEffect.ShareDaily(uri, fallbackText))
        }
    }

    fun toggleSaveDaily() {
        val card = _uiState.value.dailyCard ?: return
        viewModelScope.launch {
            if (_uiState.value.dailySaved) {
                savedCardRepository.remove(card.cardId)
            } else {
                savedCardRepository.save(
                    SavedCardEntity(
                        id = card.cardId,
                        text = card.text,
                        back = card.backSide,
                        level = card.level.name,
                        packId = card.packId,
                        packTitle = card.packTitle
                    )
                )
                _uiEffect.tryEmit(HomeScreenUIEffect.ShowMessage(R.string.saved_to_library))
            }
        }
    }

    private fun DailyCardResponse.toUi(): DailyCardUi? {
        val c = card ?: return null
        val id = c.id ?: return null
        val text = c.frontSide?.takeIf { it.isNotBlank() } ?: return null
        return DailyCardUi(
            cardId = id,
            text = text,
            backSide = c.backSide?.takeIf { it.isNotBlank() },
            level = CardLevel.from(c.level),
            packId = pack?.id ?: c.packId,
            packTitle = pack?.title,
            date = date
        )
    }
}
