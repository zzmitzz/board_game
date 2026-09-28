package com.boardgame.deepdeck.features.ingame

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.boardgame.deepdeck.features.gameend.GameEndScreen
import com.boardgame.deepdeck.features.gamesetup.GameSetupScreenStateful
import com.boardgame.deepdeck.features.gamesetup.GameSetupVM
import com.boardgame.deepdeck.features.home.HomeRoute
import com.boardgame.deepdeck.features.ingame.screen.ActiveGameScreenStateful
import com.boardgame.deepdeck.navigation.RootRoute
import com.boardgame.deepdeck.navigation.TopLevelTab
import com.boardgame.deepdeck.navigation.navigateToTab
import kotlinx.serialization.Serializable

/** Destinations inside [RootRoute.GameFlow]. */
sealed class GameFlowRoute {
    @Serializable data object Setup : GameFlowRoute()
    @Serializable data object Play : GameFlowRoute()
    @Serializable data object End : GameFlowRoute()
}

/** Opens the immersive game flow for a remote ([isCustom] = false) or My Library pack. */
fun NavController.startGame(packId: String, isCustom: Boolean = false, quickPlay: Boolean = false) {
    navigate(RootRoute.GameFlow(packId, isCustom, quickPlay)) { launchSingleTop = true }
}

/** Leaves the game flow and lands on Home (Play tab root). */
private fun NavController.exitGameToHome() {
    if (!popBackStack<HomeRoute.Main>(inclusive = false)) {
        popBackStack<RootRoute.GameFlow>(inclusive = true)
        navigateToTab(TopLevelTab.PLAY)
    }
}

/**
 * Setup → Play → End share one graph entry, so [GameSetupVM] (roster) and [InGameVM] (session,
 * scores) are scoped to the game flow instead of the whole Home graph or a global singleton.
 */
fun NavGraphBuilder.gameFlowNavigationEntry(navController: NavController) {
    navigation<RootRoute.GameFlow>(startDestination = GameFlowRoute.Setup) {

        composable<GameFlowRoute.Setup> { entry ->
            val graphEntry = navController.gameFlowEntry(entry)
            val route = remember(graphEntry) { graphEntry.toRoute<RootRoute.GameFlow>() }
            val setupVm: GameSetupVM = hiltViewModel(graphEntry)
            val gameVm: InGameVM = hiltViewModel(graphEntry)
            GameSetupScreenStateful(
                packId = route.packId,
                isCustomPack = route.isCustom,
                quickPlay = route.quickPlay,
                vm = setupVm,
                onBackClick = { navController.popBackStack() },
                onStartGame = { config ->
                    gameVm.start(route.packId, route.isCustom, config)
                    navController.navigate(GameFlowRoute.Play) { launchSingleTop = true }
                }
            )
        }

        composable<GameFlowRoute.Play>(
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) },
            popEnterTransition = { fadeIn(tween(300)) },
            popExitTransition = { fadeOut(tween(300)) }
        ) { entry ->
            val graphEntry = navController.gameFlowEntry(entry)
            val vm: InGameVM = hiltViewModel(graphEntry)
            ActiveGameScreenStateful(
                vm = vm,
                onFinished = {
                    navController.navigate(GameFlowRoute.End) {
                        popUpTo<GameFlowRoute.Play> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                // Confirmed exit mid-game: nothing is saved, back to Home (not End with partial scores).
                onQuit = { navController.exitGameToHome() }
            )
        }

        composable<GameFlowRoute.End>(
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(300)) }
        ) { entry ->
            val graphEntry = navController.gameFlowEntry(entry)
            val vm: InGameVM = hiltViewModel(graphEntry)
            GameEndScreen(
                vm = vm,
                onHomeClick = { navController.exitGameToHome() },
                onPlayAgainClick = {
                    vm.resetForPlayAgain()
                    if (!navController.popBackStack<GameFlowRoute.Setup>(inclusive = false)) {
                        navController.navigate(GameFlowRoute.Setup)
                    }
                }
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun NavController.gameFlowEntry(entry: NavBackStackEntry): NavBackStackEntry =
    remember(entry) { getBackStackEntry<RootRoute.GameFlow>() }
