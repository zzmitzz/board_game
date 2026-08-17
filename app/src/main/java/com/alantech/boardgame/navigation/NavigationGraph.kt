package com.alantech.boardgame.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alantech.boardgame.features.home.homeNavigationEntry
import com.alantech.boardgame.features.language.languageNavigationEntry
import com.alantech.boardgame.features.mylibrary.myLibraryNavigationEntry
import com.alantech.boardgame.features.setting.settingNavigationEntry
import com.alantech.boardgame.ui.state.BoardGameAppState

val LocalNavController = compositionLocalOf<NavHostController?> {
    null
}

@Composable
fun NavigationGraph(
    appState: BoardGameAppState,
) {
    val mRootNavController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides mRootNavController) {
        NavHost(
            navController = mRootNavController,
            startDestination = RootRoute.Home,
            enterTransition = {
                scaleIn(initialScale = 0.92f, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                scaleOut(targetScale = 1.08f, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                scaleIn(initialScale = 1.08f, animationSpec = tween(300)) +
                    fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                scaleOut(targetScale = 0.92f, animationSpec = tween(300)) +
                    fadeOut(animationSpec = tween(300))
            }
        ) {
            homeNavigationEntry(mRootNavController)
            settingNavigationEntry(mRootNavController)
            languageNavigationEntry(mRootNavController)
            myLibraryNavigationEntry(mRootNavController)
        }
    }
}