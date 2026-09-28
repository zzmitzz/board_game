package com.boardgame.deepdeck.features.pagedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import com.boardgame.deepdeck.data.repository.BoardGameRepository
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.PackDetailUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PageDetailUIState(
    val isLoading: Boolean = true,
    val isLoadingSampleCard: Boolean = true,
    val pack: PackDetailUIModel? = null,
    val packSampleCard: List<CardDetail> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class PageDetailVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BoardGameRepository,
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val packId: String = savedStateHandle.get<String>("id").orEmpty()

    private val _uiState = MutableStateFlow(PageDetailUIState())
    val uiState: StateFlow<PageDetailUIState> = _uiState.asStateFlow()

    init {
        retry()
    }

    fun retry() {
        loadPackDetail()
        loadSampleCards()
    }

    private fun loadPackDetail() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val pack = repository.getPackById(packId)
                _uiState.update { it.copy(isLoading = false, pack = pack) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "error") }
            }
        }
    }

    private fun loadSampleCards() {
        _uiState.update { it.copy(isLoadingSampleCard = true) }
        viewModelScope.launch {
            val setting = settingsRepository.cardSetting()
            val lang = if (setting.isAutoTranslate) setting.language else "en"
            val cards = try {
                repository.getSampleCard(packId, lang)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyList()
            }
            _uiState.update { it.copy(isLoadingSampleCard = false, packSampleCard = cards.take(3)) }
        }
    }
}
