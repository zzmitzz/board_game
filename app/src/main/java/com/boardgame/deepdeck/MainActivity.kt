package com.boardgame.deepdeck

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.ui.app.BoardGameApp
import com.boardgame.deepdeck.ui.state.rememberBoardGameState
import com.boardgame.deepdeck.ui.theme.BoardGameTheme
import com.boardgame.deepdeck.utils.LocaleUtils
import com.boardgame.deepdeck.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = applicationContext as BoardGameApplication
        app.applyStoredLocale()
        LocaleUtils.setLocale(this, app.getLanguageCode())
        super.onCreate(savedInstanceState)
        // Dark-only design: light system-bar icons on transparent bars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            val networkMonitor = remember { NetworkUtils(applicationContext) }
            val uiState = rememberBoardGameState(networkMonitor = networkMonitor)
            val hapticsEnabled by mViewModel.hapticsEnabled.collectAsStateWithLifecycle()
            BoardGameTheme {
                BoardGameApp(uiState, hapticsEnabled)
            }
        }
    }
}
