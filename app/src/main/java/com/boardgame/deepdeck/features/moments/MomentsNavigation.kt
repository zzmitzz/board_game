package com.boardgame.deepdeck.features.moments

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** Captured moments: gallery (You tab) and the full-screen viewer (gallery / End screen). */
sealed class MomentsRoute {
    @Serializable data object Gallery : MomentsRoute()
    @Serializable data class Viewer(val momentId: String) : MomentsRoute()
}

fun NavController.openMoment(momentId: String) {
    navigate(MomentsRoute.Viewer(momentId)) { launchSingleTop = true }
}

fun NavGraphBuilder.momentsNavigationEntry(navController: NavController) {
    composable<MomentsRoute.Gallery> {
        MomentsGalleryScreen(
            vm = hiltViewModel(),
            onBackClick = { navController.popBackStack() },
            onOpenMoment = { navController.openMoment(it) }
        )
    }
    composable<MomentsRoute.Viewer> {
        MomentViewerScreen(
            vm = hiltViewModel(),
            onBackClick = { navController.popBackStack() }
        )
    }
}
