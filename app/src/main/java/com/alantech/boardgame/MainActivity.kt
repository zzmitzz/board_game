package com.alantech.boardgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alantech.boardgame.navigation.NavigationGraph
import com.alantech.boardgame.ui.app.BoardGameApp
import com.alantech.boardgame.ui.state.BoardGameAppState
import com.alantech.boardgame.ui.state.rememberBoardGameState
import com.alantech.boardgame.ui.theme.BoardGameTheme
import com.alantech.boardgame.utils.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking


import com.alantech.boardgame.utils.LocaleUtils

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mViewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = applicationContext as BoardGameApplication
        app.applyStoredLocale()
        LocaleUtils.setLocale(this, app.getLanguageCode())
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState = rememberBoardGameState(
                networkMonitor = NetworkUtils(this)
            )
            BoardGameTheme {
                BoardGameApp(uiState)
            }
        }
    }
}
