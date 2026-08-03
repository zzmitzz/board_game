package com.alantech.boardgame.features.setting

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.alantech.boardgame.features.playhistory.PlayHistoryScreen
import com.alantech.boardgame.features.playhistory.PlayHistoryVM
import com.alantech.boardgame.navigation.RootRoute
import kotlinx.serialization.Serializable


sealed class SettingRoute {
    @Serializable
    data object Setting : SettingRoute()

    @Serializable
    data object PlayHistory : SettingRoute()
}

fun NavGraphBuilder.settingNavigationEntry(
    navController: NavController
) {
    navigation<RootRoute.Setting>(
        startDestination = SettingRoute.Setting) {
        composable<SettingRoute.Setting>(
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
            SettingScreen(
                onBackClick = { navController.popBackStack() },
                onLanguageClick = { navController.navigate(RootRoute.Language) },
                onPlayHistoryClick = { navController.navigate(SettingRoute.PlayHistory) }
            )
        }
        composable<SettingRoute.PlayHistory>(
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
            val vm: PlayHistoryVM = hiltViewModel()
            PlayHistoryScreen(
                vm = vm,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}