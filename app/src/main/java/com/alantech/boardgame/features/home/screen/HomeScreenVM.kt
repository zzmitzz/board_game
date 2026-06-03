package com.alantech.boardgame.features.home.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.remote.BoardGameRepository
import com.alantech.boardgame.di.RepositoryProvider
import com.alantech.boardgame.ui.model.CardPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeScreenUIState(
    val isTrendingComponentLoading: Boolean = false,
    val trendingPacks: List<CardPreview> = emptyList(),
    val errorMessage: String? = null
)

class HomeScreenVM(
    private val repository: BoardGameRepository = RepositoryProvider.boardGameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUIState())
    val uiState: StateFlow<HomeScreenUIState> = _uiState.asStateFlow()

    init {
        loadTrendingPacks()
    }

    fun loadTrendingPacks() {
        _uiState.update { it.copy(isTrendingComponentLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val packs = repository.getPacks()
                _uiState.update {
                    it.copy(
                        isTrendingComponentLoading = false,
                        trendingPacks = packs
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTrendingComponentLoading = false,
                        errorMessage = e.message ?: "Failed to load packs"
                    )
                }
            }
        }
    }
}