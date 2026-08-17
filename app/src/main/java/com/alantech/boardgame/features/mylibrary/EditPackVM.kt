package com.alantech.boardgame.features.mylibrary

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.local.entity.LocalPackEntity
import com.alantech.boardgame.data.repository.LocalLibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPackVM @Inject constructor(
    private val repository: LocalLibraryRepository
) : ViewModel() {

    private val _form = MutableStateFlow(AddPackFormState())
    val form: StateFlow<AddPackFormState> = _form.asStateFlow()

    private var originalId: String = ""

    fun loadPack(packId: String) {
        viewModelScope.launch {
            val pack = repository.getAllPacks().first().find { it.id == packId } ?: return@launch
            originalId = pack.id
            _form.value = AddPackFormState(
                title = pack.title,
                description = pack.description,
                tag = pack.tag,
                heatLevel = pack.heatLevel,
                estimateTimePlay = pack.estimateTimePlay,
                suggestNumberPlayers = pack.suggestNumberPlayers,
                howToPlay = pack.howToPlay,
                coverImageUri = pack.coverImageUri?.let { Uri.parse(it) }
            )
        }
    }

    fun onTitleChange(value: String) = _form.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _form.update { it.copy(description = value) }
    fun onTagChange(value: String) = _form.update { it.copy(tag = value) }
    fun onHeatLevelChange(value: Int) = _form.update { it.copy(heatLevel = value) }
    fun onEstimateTimeChange(value: Int) = _form.update { it.copy(estimateTimePlay = value) }
    fun onSuggestPlayersChange(value: Int) = _form.update { it.copy(suggestNumberPlayers = value) }
    fun onHowToPlayChange(value: String) = _form.update { it.copy(howToPlay = value) }
    fun onCoverImageSelected(uri: Uri?) = _form.update { it.copy(coverImageUri = uri) }

    fun save(onSuccess: () -> Unit) {
        val state = _form.value
        if (state.title.isBlank() || originalId.isEmpty()) return
        _form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.createPack(
                LocalPackEntity(
                    id = originalId,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    coverImageUri = state.coverImageUri?.toString(),
                    tag = state.tag.trim(),
                    heatLevel = state.heatLevel,
                    estimateTimePlay = state.estimateTimePlay,
                    suggestNumberPlayers = state.suggestNumberPlayers,
                    howToPlay = state.howToPlay.trim()
                )
            )
            _form.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
