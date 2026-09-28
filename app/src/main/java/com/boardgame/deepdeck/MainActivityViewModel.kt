package com.boardgame.deepdeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    settingsRepository: AppSettingsRepository,
) : ViewModel() {

    /** Drives UI haptics (press / confirm …) app-wide. */
    val hapticsEnabled: StateFlow<Boolean> = settingsRepository.settings
        .map { it.hapticsEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
