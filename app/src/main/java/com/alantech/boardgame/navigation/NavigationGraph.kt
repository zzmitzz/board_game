package com.alantech.boardgame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alantech.boardgame.features.home.homeNavigationEntry
import com.alantech.boardgame.features.onboard.onBoardingGraph
import com.alantech.boardgame.ui.state.BoardGameAppState


@Composable
fun NavigationGraph(
    appState: BoardGameAppState,
) {
    val mRootNavController = rememberNavController()
    NavHost(
        navController = mRootNavController,
        startDestination = RootRoute.Home
    ) {
        onBoardingGraph(mRootNavController)
        homeNavigationEntry(mRootNavController)
    }
}