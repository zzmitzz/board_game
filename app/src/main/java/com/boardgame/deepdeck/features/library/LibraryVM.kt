package com.boardgame.deepdeck.features.library

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.local.entity.GameResult
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.data.repository.LocalLibraryRepository
import com.boardgame.deepdeck.data.repository.SavedCardRepository
import com.boardgame.deepdeck.features.ingame.engine.Awards
import com.boardgame.deepdeck.features.ingame.engine.LeaderboardEntry
import com.boardgame.deepdeck.utils.share.ShareCardData
import com.boardgame.deepdeck.utils.share.ShareImageRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomPackUi(
    val pack: LocalPackEntity,
    val cardCount: Int,
)

data class HistoryItemUi(
    val result: GameResult,
    val ranked: List<LeaderboardEntry>,
    val isCustomPack: Boolean,
    /** Pack still exists (remote packs are assumed to). */
    val canReplay: Boolean,
)

data class LibraryUiState(
    val saved: List<SavedCardEntity> = emptyList(),
    val packs: List<CustomPackUi> = emptyList(),
    val history: List<HistoryItemUi> = emptyList(),
    val historyLoaded: Boolean = false,
)

sealed interface LibraryUiEffect {
    data class Share(val uri: Uri?, val fallbackText: String) : LibraryUiEffect
    data class CardRemoved(val card: SavedCardEntity) : LibraryUiEffect
    data class ShowMessage(@StringRes val message: Int) : LibraryUiEffect
}

/** Library tab (plan §7.2.11): Saved ♥ · My packs · History. */
@HiltViewModel
class LibraryVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedCardRepository: SavedCardRepository,
    private val localLibraryRepository: LocalLibraryRepository,
    private val gameResultRepository: GameResultRepository,
    private val shareImageRenderer: ShareImageRenderer,
) : ViewModel() {

    private val history = MutableStateFlow<List<GameResult>?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        savedCardRepository.observeAll(),
        localLibraryRepository.getAllPacks(),
        localLibraryRepository.observeCardCounts(),
        history,
    ) { saved, packs, counts, results ->
        val localIds = packs.map { it.id }.toSet()
        LibraryUiState(
            saved = saved,
            packs = packs.map { CustomPackUi(it, counts[it.id] ?: 0) },
            history = results.orEmpty()
                .filter { it.gameScore.isNotEmpty() }
                .sortedByDescending { it.timeStamp }
                .map { result ->
                    val isCustom = result.packID in localIds
                    HistoryItemUi(
                        result = result,
                        ranked = Awards.rank(result.gameScore),
                        isCustomPack = isCustom,
                        // A deleted custom pack can't be told apart from a remote id; replaying it
                        // lands on the in-game error state (with Back) instead of crashing.
                        canReplay = result.packID.isNotBlank(),
                    )
                },
            historyLoaded = results != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    private val _uiEffect = MutableSharedFlow<LibraryUiEffect>(extraBufferCapacity = 4)
    val uiEffect: SharedFlow<LibraryUiEffect> = _uiEffect.asSharedFlow()

    /** History comes from a suspend DAO call; refreshed when the tab resumes. */
    fun refreshHistory() {
        viewModelScope.launch {
            history.value = runCatching { gameResultRepository.getAll() }.getOrDefault(emptyList())
        }
    }

    fun shareSaved(card: SavedCardEntity) {
        viewModelScope.launch {
            val uri = shareImageRenderer.renderCard(
                ShareCardData(card.text, CardLevel.from(card.level), card.packTitle)
            )
            _uiEffect.emit(
                LibraryUiEffect.Share(uri, "“${card.text}”\n\n${context.getString(R.string.share_daily_footer)}")
            )
        }
    }

    fun removeSaved(card: SavedCardEntity) {
        viewModelScope.launch {
            savedCardRepository.remove(card.id)
            _uiEffect.emit(LibraryUiEffect.CardRemoved(card))
        }
    }

    /** Undo for [removeSaved]; no LOVE feedback is re-sent. */
    fun restoreSaved(card: SavedCardEntity) {
        viewModelScope.launch { savedCardRepository.save(card, sendLove = false) }
    }

    fun deletePack(pack: LocalPackEntity) {
        viewModelScope.launch {
            localLibraryRepository.deletePack(pack)
            _uiEffect.emit(LibraryUiEffect.ShowMessage(R.string.pack_deleted))
        }
    }
}
