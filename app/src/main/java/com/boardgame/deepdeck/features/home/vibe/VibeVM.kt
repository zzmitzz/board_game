package com.boardgame.deepdeck.features.home.vibe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.repository.HomeDataRepository
import com.boardgame.deepdeck.ui.components.normalizeHeat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VibeUiState {
    data object Loading : VibeUiState
    data class Success(val packs: List<PacksPreview>) : VibeUiState
    data class Error(val message: String) : VibeUiState
}

/** Packs of one vibe, hottest first. */
@HiltViewModel
class VibeVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val homeDataRepository: HomeDataRepository,
) : ViewModel() {

    private val vibeId: String = savedStateHandle.get<String>("vibeId").orEmpty()

    private val _uiState = MutableStateFlow<VibeUiState>(VibeUiState.Loading)
    val uiState: StateFlow<VibeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = VibeUiState.Loading
        viewModelScope.launch {
            homeDataRepository.getCardsWithVibe(vibeId)
                .onSuccess { packs ->
                    _uiState.value = VibeUiState.Success(
                        packs.filter { !it.id.isNullOrBlank() }
                            .sortedWith(
                                compareByDescending<PacksPreview> { normalizeHeat(it.heatLevel) }
                                    .thenBy { it.title.orEmpty() }
                            )
                    )
                }
                .onFailure { _uiState.value = VibeUiState.Error(it.message.orEmpty()) }
        }
    }
}
