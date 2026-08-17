package com.alantech.boardgame.features.mylibrary

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alantech.boardgame.data.local.entity.LocalCardEntity
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
class EditCardVM @Inject constructor(
    private val repository: LocalLibraryRepository
) : ViewModel() {

    private val _form = MutableStateFlow(AddCardFormState())
    val form: StateFlow<AddCardFormState> = _form.asStateFlow()

    private var originalPackId: String = ""
    private var originalId: String = ""

    fun loadCard(packId: String, cardId: String) {
        originalPackId = packId
        viewModelScope.launch {
            val card = repository.getCardsForPack(packId).first().find { it.id == cardId } ?: return@launch
            originalId = card.id
            _form.value = AddCardFormState(
                category = card.category,
                description = card.description,
                hint = card.hint,
                mediaImageUri = card.mediaImageUri?.let { Uri.parse(it) }
            )
        }
    }

    fun onCategoryChange(value: String) = _form.update { it.copy(category = value) }
    fun onDescriptionChange(value: String) = _form.update { it.copy(description = value) }
    fun onHintChange(value: String) = _form.update { it.copy(hint = value) }
    fun onImageSelected(uri: Uri?) = _form.update { it.copy(mediaImageUri = uri) }

    fun save(onSuccess: () -> Unit) {
        val state = _form.value
        if (state.description.isBlank() || originalId.isEmpty()) return
        _form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.createCard(
                LocalCardEntity(
                    id = originalId,
                    packId = originalPackId,
                    category = state.category.trim(),
                    description = state.description.trim(),
                    hint = state.hint.trim(),
                    mediaImageUri = state.mediaImageUri?.toString()
                )
            )
            _form.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
