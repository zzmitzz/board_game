package com.boardgame.deepdeck.features.language

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.boardgame.deepdeck.navigation.RootRoute

fun NavGraphBuilder.languageNavigationEntry(
    navController: NavController,
) {
    composable<RootRoute.Language> {
        val vm = hiltViewModel<LanguageSelectVM>()
        LanguageSelectScreen(
            vm = vm,
            onBackClick = { navController.popBackStack() }
        )
    }
}
