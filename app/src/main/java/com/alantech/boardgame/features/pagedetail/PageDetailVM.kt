package com.alantech.boardgame.features.pagedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.features.ingame.InGameVM.Companion.PREF_USER_SETTING
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.PackDetailUIModel
import com.alantech.boardgame.utils.DataStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PageDetailUIState(
    val isLoading: Boolean = false,
    val isLoadingSampleCard: Boolean = false,
    val pack: PackDetailUIModel? = null,
    val packSampleCard: List<CardDetail>? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class PageDetailVM @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: BoardGameRepository,
    private val dataStoreUtils: DataStoreUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(PageDetailUIState())
    val uiState: StateFlow<PageDetailUIState> = _uiState.asStateFlow()
    init {
        loadPackDetail()
        loadSampleCardPack()
    }

    private fun loadPackDetail() {
        val id = savedStateHandle.get<String>("id").orEmpty()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val pack = repository.getPackById(id)
                _uiState.update { it.copy(isLoading = false, pack = pack) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load pack") }
            }
        }
    }
    private suspend fun currentSetting(): PersistenceSetting {
        return dataStoreUtils.getSerializedData(PREF_USER_SETTING, PersistenceSetting::class.java)
            ?: PersistenceSetting()
    }
    private fun loadSampleCardPack(){
        val id = savedStateHandle.get<String>("id").orEmpty()
        _uiState.update{
            it.copy(isLoadingSampleCard = true, errorMessage = null)
        }
        viewModelScope.launch {
            val setting = currentSetting()
            try {
                val pack = repository.getSampleCard(id, setting.language)
                _uiState.update { it.copy(isLoadingSampleCard = false, packSampleCard = pack) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingSampleCard = false, errorMessage = e.message ?: "Failed to load pack") }
            }
        }
    }
}