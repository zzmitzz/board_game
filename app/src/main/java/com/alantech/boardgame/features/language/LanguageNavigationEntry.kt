package com.alantech.boardgame.features.language

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alantech.boardgame.navigation.RootRoute

fun NavGraphBuilder.languageNavigationEntry(
    navController: NavController,
) {
    composable<RootRoute.Language>(
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
        val vm = hiltViewModel<LanguageSelectVM>()
        LanguageSelectScreen(
            vm = vm,
            onBackClick = { navController.popBackStack() }
        )
    }
}
