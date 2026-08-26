package com.boardgame.deepdeck.features.mylibrary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import com.boardgame.deepdeck.data.repository.LocalLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyLibraryVM @Inject constructor(
    private val repository: LocalLibraryRepository
) : ViewModel() {

    val packs: StateFlow<List<LocalPackEntity>> = repository.getAllPacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deletePack(pack: LocalPackEntity) {
        viewModelScope.launch { repository.deletePack(pack) }
    }
}
