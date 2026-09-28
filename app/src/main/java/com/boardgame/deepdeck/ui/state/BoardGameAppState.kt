package com.boardgame.deepdeck.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.boardgame.deepdeck.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


/** Remembered across recompositions (previously re-created on every recomposition). */
@Composable
fun rememberBoardGameState(
    networkMonitor: NetworkUtils,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): BoardGameAppState = remember(networkMonitor, coroutineScope) {
    BoardGameAppState(coroutineScope, networkMonitor)
}

@Stable
class BoardGameAppState(
    val coroutineScope: CoroutineScope,
    val networkMonitor: NetworkUtils
) {
    val isOffline: StateFlow<Boolean> = networkMonitor.observeNetworkState()
        .map { isOnline -> !isOnline }
        .distinctUntilChanged()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = !networkMonitor.isNetworkConnected()
        )
}
