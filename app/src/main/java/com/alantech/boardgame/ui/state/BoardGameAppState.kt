package com.alantech.boardgame.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.alantech.boardgame.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


@Composable
fun rememberBoardGameState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    networkMonitor: NetworkUtils
): BoardGameAppState {
    return BoardGameAppState(
        coroutineScope,
        networkMonitor
    )
}


class BoardGameAppState(
    val coroutineScope: CoroutineScope,
    val networkMonitor: NetworkUtils
) {
    val isOffline = networkMonitor.observeNetworkState()
        .map { isOnline -> !isOnline }
        .stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = !networkMonitor.isNetworkConnected()
    )

    
}