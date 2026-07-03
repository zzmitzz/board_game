package com.alantech.boardgame.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alantech.boardgame.R
import com.alantech.boardgame.navigation.NavigationGraph
import com.alantech.boardgame.ui.state.BoardGameAppState
import com.alantech.boardgame.ui.theme.LightBackground


val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@Composable
fun BoardGameApp(
    appState: BoardGameAppState
) {
    val snackBarState = remember { SnackbarHostState() }
    val offlineMessage = stringResource(R.string.network_connection_fail)
    LaunchedEffect(appState.isOffline.collectAsStateWithLifecycle()) {
        if (appState.isOffline.value) {
            snackBarState.showSnackbar(
                offlineMessage,
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackBarState
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBarState)
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        color = LightBackground
                    )
            ){
                NavigationGraph(
                    appState
                )
            }
        }
    }
}