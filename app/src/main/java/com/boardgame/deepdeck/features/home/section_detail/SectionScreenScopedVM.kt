package com.boardgame.deepdeck.features.home.section_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.SectionEntity
import com.boardgame.deepdeck.data.repository.HomeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class SectionUIState {
    data object Loading : SectionUIState()
    data class Success(val sectionEntity: SectionEntity?, val packs: List<PacksPreview>) : SectionUIState()
    data class Error(val message: String) : SectionUIState()
}

@HiltViewModel
class SectionScreenScopedVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val homeDataRepository: HomeDataRepository,
) : ViewModel() {
    private val sectionID: String? = savedStateHandle.get<String>("sectionID")

    private val _uiState = MutableStateFlow<SectionUIState>(SectionUIState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = sectionID
        if (id.isNullOrBlank()) {
            _uiState.value = SectionUIState.Error("Missing section id")
            return
        }
        _uiState.value = SectionUIState.Loading
        viewModelScope.launch {
            val detail = async { homeDataRepository.getSectionDetail(id) }
            val packs = homeDataRepository.getSectionPacks(id)
            _uiState.value = packs.fold(
                onSuccess = { list ->
                    SectionUIState.Success(
                        detail.await().getOrNull(),
                        list.filter { !it.id.isNullOrBlank() }
                    )
                },
                onFailure = { SectionUIState.Error(it.message.orEmpty()) }
            )
        }
    }
}
