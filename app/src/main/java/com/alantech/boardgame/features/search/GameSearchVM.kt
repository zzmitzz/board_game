package com.alantech.boardgame.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.ui.model.PackDetailUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



sealed class SealedGameSearchUIState {

    data class GameSearchUIState(
        val query: String = "",
        val results: List<PacksPreview> = emptyList(),
    ) : SealedGameSearchUIState()

    data class InitUIState(
        val recentSearch: List<String> = emptyList(),
        val suggestPacks: List<PacksPreview> = emptyList()
    ) : SealedGameSearchUIState()

    object Loading : SealedGameSearchUIState()

    data class Error(
        val message: String
    ) : SealedGameSearchUIState()
}


@HiltViewModel
class GameSearchVM @Inject constructor(
    private val repository: BoardGameRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SealedGameSearchUIState> = MutableStateFlow(SealedGameSearchUIState.Loading)
    val uiState: StateFlow<SealedGameSearchUIState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private var cachedInitState: SealedGameSearchUIState.InitUIState = SealedGameSearchUIState.InitUIState()

    init {
        loadInitData()
        observeQuery()
    }

    fun onQueryChange(query: String) {
        _query.update { query }
    }

    fun clearQuery() {
        _query.update { "" }
        _uiState.update { cachedInitState }
    }

    private fun loadInitData() {
        _uiState.update { SealedGameSearchUIState.Loading }
        viewModelScope.launch {
            try {
                val recentSearch = repository.getRecentSearch()
                cachedInitState = cachedInitState.copy(recentSearch = recentSearch)
                _uiState.update { cachedInitState }
            } catch (e: Exception) {
            }
        }

        viewModelScope.launch {
            try {
                val suggestPack = repository.getSuggestPacks()
                cachedInitState = cachedInitState.copy(suggestPacks = suggestPack)
                _uiState.update { current ->
                    if (current is SealedGameSearchUIState.InitUIState) cachedInitState else current
                }
            } catch (e: Exception) {
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        viewModelScope.launch {
            _query
                .debounce(300L)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update { cachedInitState }
                    } else {
                        _uiState.update { SealedGameSearchUIState.Loading }
                        val results = runCatching { repository.searchPacksByName(query) }.getOrElse { emptyList() }
                        _uiState.update {
                            SealedGameSearchUIState.GameSearchUIState(query = query, results = results)
                        }
                    }
                }
        }
    }
}
