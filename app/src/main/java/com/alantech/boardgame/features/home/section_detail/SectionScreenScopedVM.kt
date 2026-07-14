package com.alantech.boardgame.features.home.section_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.SectionEntity
import com.alantech.boardgame.data.repository.HomeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class SectionUIState(){
    object Loading : SectionUIState()
    data class Success(val sectionEntity: SectionEntity, val packs: List<PacksPreview>) : SectionUIState()
    data class Error(val message: String) : SectionUIState()
}

@HiltViewModel
class SectionScreenScopedVM @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val homeDataRepository: HomeDataRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SectionUIState>(SectionUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadingInitData()
    }

    private fun loadingInitData(){
        val sectionID: String? = savedStateHandle.get<String>(key = "sectionID")
        if(sectionID == null){
            _uiState.value = SectionUIState.Error("Section ID is null")
            return
        }
        viewModelScope.launch {
            val sectionDetail = homeDataRepository.getSectionDetail(sectionID)
            val packs = homeDataRepository.getSectionPacks(sectionID)
            _uiState.value = SectionUIState.Success(sectionDetail.getOrNull() ?: SectionEntity(), packs.getOrNull() ?: emptyList())
        }
    }
}