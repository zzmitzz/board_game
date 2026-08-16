package com.alantech.boardgame.features.home.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.SectionEntity
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.data.repository.HomeDataRepository
import com.alantech.boardgame.features.home.model.VibeChip
import com.alantech.boardgame.ui.model.PackDetailUIModel
import com.alantech.boardgame.ui.model.mockVibeChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeScreenUIState(
    val isTrendingComponentLoading: Boolean = false,
    val isVibeComponentLoading: Boolean = false,
    val listVibePacks: List<VibeChip> = emptyList(),
    val currentSelectedVibeChip: VibeChip? = null,
    val isSelectedVibeLoading: Boolean = false,
    val listPacksFromVibe: List<PacksPreview> = emptyList(),
    val sectionPacks: Map<SectionEntity, List<PacksPreview>> = emptyMap(),
    val errorMessage: String? = null,
)

sealed class HomeScreenUIEffect {
    data class ShowError(val message: String) : HomeScreenUIEffect()
}

@HiltViewModel
class HomeScreenVM @Inject constructor(
    private val homeDataRepository: HomeDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUIState())
    val uiState: StateFlow<HomeScreenUIState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeScreenUIEffect?>(0)
    val uiEffect: SharedFlow<HomeScreenUIEffect?> = _uiEffect.asSharedFlow()

    companion object {
        private const val TAG = "HomeScreenVM"
    }

    init {
        loadAllSectionsPacks()
        loadVibePacks()
    }

    fun refreshAllData() {
        _uiState.update {
            HomeScreenUIState()
        }
        loadAllSectionsPacks()
        loadVibePacks()
    }

    private fun loadVibePacks() {
        // 1. Atomically set loading to true
        _uiState.update { it.copy(isVibeComponentLoading = true) }

        viewModelScope.launch {
            try {
                val vibes = homeDataRepository.getAllVibesData()
                if (vibes.isSuccess) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isVibeComponentLoading = false,
                            listVibePacks = vibes.getOrNull().orEmpty()
                        )
                    }
                } else if (vibes.isFailure) {
                    val errorMsg = vibes.exceptionOrNull()?.message ?: "Failed to load vibes"
                    _uiState.update { currentState ->
                        currentState.copy(
                            isVibeComponentLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                    showErrorToast(errorMsg)
                }
            } catch (e: Exception) {
                showErrorToast(e.message ?: "Failed to load vibes")
            }
        }
    }

    private fun showErrorToast(message: String) {
        viewModelScope.launch {
            _uiEffect.emit(HomeScreenUIEffect.ShowError(message))
        }
    }

    private fun loadAllSectionsPacks() {
        _uiState.update { it.copy(isTrendingComponentLoading = true) }

        viewModelScope.launch {
            // Dispatchers.IO is correct for heavy networking/DB work
            withContext(Dispatchers.IO) {
                try {
                    val sectionResult = homeDataRepository.getSections()
                    if (sectionResult.isSuccess) {
                        val mapData = mutableMapOf<SectionEntity, List<PacksPreview>>()
                        val packs = sectionResult.getOrNull().orEmpty()

                        // Concurrent fetching using async
                        packs.map { section ->
                            async {
                                val sectionPacks =
                                    homeDataRepository.getSectionPacks(section.id!!).getOrNull()
                                        .orEmpty()
                                // Synchronize map access because multiple async blocks write to it concurrently
                                synchronized(mapData) {
                                    mapData[section] = sectionPacks
                                }
                            }
                        }.awaitAll()

                        val sortedPacks = mapData.toSortedMap { section1, section2 ->
                            (section1.displayOrder ?: 0).compareTo(section2.displayOrder ?: 0)
                        }

                        // 3. Atomically update the state with sorted section packs
                        _uiState.update { currentState ->
                            currentState.copy(
                                isTrendingComponentLoading = false,
                                sectionPacks = sortedPacks
                            )
                        }
                    } else if (sectionResult.isFailure) {
                        val errorMsg =
                            sectionResult.exceptionOrNull()?.message ?: "Failed to load packs"
                        _uiState.update { currentState ->
                            currentState.copy(
                                isTrendingComponentLoading = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message ?: "Failed to load packs")
                }
            }
        }
    }

    private suspend fun updateUIState(newState: HomeScreenUIState) {
        withContext(Dispatchers.Main.immediate) {
            _uiState.update { newState }
        }
    }

    fun selectVibeChip(
        chipID: String
    ) {
        if (_uiState.value.listVibePacks.isEmpty()) {
            return
        }

        val listVibeChip = _uiState.value.listVibePacks
        val selectedChip = listVibeChip.find { chip -> chip.id == chipID }

        if (selectedChip == _uiState.value.currentSelectedVibeChip) {
            _uiState.update {
                it.copy(currentSelectedVibeChip = null, listPacksFromVibe = listOf())
            }
            return
        }
        _uiState.update {
            it.copy(
                currentSelectedVibeChip = selectedChip,
                listPacksFromVibe = listOf(),
                isSelectedVibeLoading = true
            )
        }
        viewModelScope.launch {
            selectedChip?.let { vibe ->
                val packsData = homeDataRepository.getCardsWithVibe(vibe.id).getOrNull() ?: listOf()
                _uiState.update {
                    it.copy(listPacksFromVibe = packsData, isSelectedVibeLoading = false)
                }
            }
        }

    }
}