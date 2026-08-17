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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddCardFormState(
    val category: String = "",
    val description: String = "",
    val hint: String = "",
    val mediaImageUri: Uri? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class AddCardVM @Inject constructor(
    private val repository: LocalLibraryRepository
) : ViewModel() {

    private val _form = MutableStateFlow(AddCardFormState())
    val form: StateFlow<AddCardFormState> = _form.asStateFlow()

    fun onCategoryChange(value: String) = _form.update { it.copy(category = value) }
    fun onDescriptionChange(value: String) = _form.update { it.copy(description = value) }
    fun onHintChange(value: String) = _form.update { it.copy(hint = value) }
    fun onImageSelected(uri: Uri?) = _form.update { it.copy(mediaImageUri = uri) }

    fun save(packId: String, onSuccess: () -> Unit) {
        val state = _form.value
        if (state.description.isBlank()) return
        _form.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.createCard(
                LocalCardEntity(
                    id = UUID.randomUUID().toString(),
                    packId = packId,
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
