package com.alantech.boardgame.features.mylibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.local.entity.LocalCardEntity
import com.alantech.boardgame.data.local.entity.LocalPackEntity
import com.alantech.boardgame.data.repository.LocalLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PackCardsVM @Inject constructor(
    private val repository: LocalLibraryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packId: String = checkNotNull(savedStateHandle["packId"])

    val pack: StateFlow<LocalPackEntity?> = repository.getAllPacks()
        .map { list -> list.find { it.id == packId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val cards: StateFlow<List<LocalCardEntity>> = repository.getCardsForPack(packId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteCard(card: LocalCardEntity) {
        viewModelScope.launch { repository.deleteCard(card) }
    }
}

