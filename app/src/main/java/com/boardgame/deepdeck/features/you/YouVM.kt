package com.boardgame.deepdeck.features.you

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.prefs.AppSettings
import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import com.boardgame.deepdeck.data.prefs.DailyStreakRepository
import com.boardgame.deepdeck.data.prefs.StreakState
import com.boardgame.deepdeck.work.EveningReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YouUiState(
    val settings: AppSettings = AppSettings(),
    val streak: StreakState = StreakState(),
    val gamesPlayed: Int = 0,
)

@HiltViewModel
class YouVM @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: AppSettingsRepository,
    streakRepository: DailyStreakRepository,
    private val gameResultRepository: GameResultRepository,
) : ViewModel() {

    private val gamesPlayed = MutableStateFlow(0)

    val uiState: StateFlow<YouUiState> = combine(
        settingsRepository.settings,
        streakRepository.streak,
        gamesPlayed
    ) { settings, streak, games -> YouUiState(settings, streak, games) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), YouUiState())

    fun refreshStats() {
        viewModelScope.launch {
            gamesPlayed.value = runCatching { gameResultRepository.getAll().size }.getOrDefault(0)
        }
    }

    fun setSound(enabled: Boolean) = viewModelScope.launch { settingsRepository.setSound(enabled) }

    fun setHaptics(enabled: Boolean) = viewModelScope.launch { settingsRepository.setHaptics(enabled) }

    /** Evening reminder: persists the opt-in and (un)schedules the 20:00 WorkManager job. */
    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setNotifications(enabled)
        if (enabled) EveningReminder.schedule(context, replace = true) else EveningReminder.cancel(context)
    }
}
