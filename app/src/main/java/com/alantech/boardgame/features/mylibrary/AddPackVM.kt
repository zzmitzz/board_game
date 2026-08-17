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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddPackFormState(
    val title: String = "",
    val description: String = "",
    val tag: String = "",
    val heatLevel: Int = 1,
    val estimateTimePlay: Int = 30,
    val suggestNumberPlayers: Int = 2,
    val howToPlay: String = "",
    val coverImageUri: Uri? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class AddPackVM @Inject constructor(
    private val repository: LocalLibraryRepository
) : ViewModel() {

    private val _form = MutableStateFlow(AddPackFormState())
    val form: StateFlow<AddPackFormState> = _form.asStateFlow()

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
        if (state.title.isBlank()) return
        _form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.createPack(
                LocalPackEntity(
                    id = UUID.randomUUID().toString(),
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
