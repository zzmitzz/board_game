package com.alantech.boardgame.features.playhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.local.GameResultRepository
import com.alantech.boardgame.data.local.entity.GameResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayHistoryVM @Inject constructor(
    private val gameResultRepository: GameResultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<GameResult>>(emptyList())
    val uiState: StateFlow<List<GameResult>> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = gameResultRepository.getAll().sortedByDescending { it.timeStamp }
        }
    }
}
