package com.boardgame.deepdeck.features.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import com.boardgame.deepdeck.utils.LanguageItem
import com.boardgame.deepdeck.utils.listLanguageSupport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageUiState(
    val selected: LanguageItem = listLanguageSupport.first(),
    val translateCards: Boolean = true,
    val initialCode: String = "en",
    val initialTranslate: Boolean = true,
) {
    val hasChanges: Boolean get() = selected.code != initialCode || translateCards != initialTranslate
}

/** Unified language: UI language + "Translate cards to my language" in one place. */
@HiltViewModel
class LanguageSelectVM @Inject constructor(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = settingsRepository.current()
            val item = listLanguageSupport.find { it.code == current.uiLanguage } ?: listLanguageSupport.first()
            val translate = current.translateCards || current.uiLanguage == "en"
            _uiState.value = LanguageUiState(item, translate, item.code, translate)
        }
    }

    fun select(item: LanguageItem) = _uiState.update { it.copy(selected = item) }

    fun setTranslateCards(enabled: Boolean) = _uiState.update { it.copy(translateCards = enabled) }

    /** Persists both prefs; [onLocaleChanged] is invoked when the UI language changed (recreate). */
    fun save(onDone: (localeChanged: Boolean, code: String) -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            settingsRepository.setLanguage(state.selected.code, state.translateCards)
            onDone(state.selected.code != state.initialCode, state.selected.code)
        }
    }
}
