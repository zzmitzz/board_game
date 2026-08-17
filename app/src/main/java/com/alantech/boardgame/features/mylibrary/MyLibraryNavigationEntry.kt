package com.alantech.boardgame.features.mylibrary

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.alantech.boardgame.features.gameend.GameEndScreen
import com.alantech.boardgame.features.gamesetup.GameSetupScreenStateful
import com.alantech.boardgame.features.gamesetup.GameSetupVM
import com.alantech.boardgame.features.home.shareviewmodel.shareViewModel
import com.alantech.boardgame.features.ingame.screen.ActiveGameScreenStateful
import com.alantech.boardgame.navigation.RootRoute
import kotlinx.serialization.Serializable

sealed class MyLibraryRoute {
    @Serializable data object Library : MyLibraryRoute()
    @Serializable data object AddPack : MyLibraryRoute()
    @Serializable data class PackCards(val packId: String) : MyLibraryRoute()
    @Serializable data class AddCard(val packId: String) : MyLibraryRoute()
    @Serializable data class EditPack(val packId: String) : MyLibraryRoute()
    @Serializable data class EditCard(val packId: String, val cardId: String) : MyLibraryRoute()
    @Serializable data class GameSetup(val packId: String) : MyLibraryRoute()
    @Serializable data class InGame(val packId: String) : MyLibraryRoute()
    @Serializable data object EndGame : MyLibraryRoute()
}

private fun enter() = scaleIn(initialScale = 0.92f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
private fun exit() = scaleOut(targetScale = 1.08f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
private fun popEnter() = scaleIn(initialScale = 1.08f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
private fun popExit() = scaleOut(targetScale = 0.92f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))

fun NavGraphBuilder.myLibraryNavigationEntry(navController: NavController) {
    navigation<RootRoute.MyLibrary>(startDestination = MyLibraryRoute.Library) {

        composable<MyLibraryRoute.Library>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) {
            val vm: MyLibraryVM = hiltViewModel()
            MyLibraryScreen(
                vm = vm,
                onBackClick = { navController.popBackStack() },
                onAddPackClick = { navController.navigate(MyLibraryRoute.AddPack) },
                onPackClick = { packId -> navController.navigate(MyLibraryRoute.PackCards(packId)) },
                onEditPackClick = { packId -> navController.navigate(MyLibraryRoute.EditPack(packId)) }
            )
        }

        composable<MyLibraryRoute.AddPack>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) {
            val vm: AddPackVM = hiltViewModel()
            AddPackScreen(
                vm = vm,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.PackCards>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.PackCards>()
            val vm: PackCardsVM = hiltViewModel()
            PackCardsScreen(
                vm = vm,
                onBackClick = { navController.popBackStack() },
                onAddCardClick = { navController.navigate(MyLibraryRoute.AddCard(route.packId)) },
                onEditCardClick = { cardId -> navController.navigate(MyLibraryRoute.EditCard(route.packId, cardId)) },
                onPlayClick = { navController.navigate(MyLibraryRoute.GameSetup(route.packId)) }
            )
        }

        composable<MyLibraryRoute.AddCard>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.AddCard>()
            val vm: AddCardVM = hiltViewModel()
            AddCardScreen(
                vm = vm,
                packId = route.packId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.EditPack>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.EditPack>()
            val vm: EditPackVM = hiltViewModel()
            EditPackScreen(
                vm = vm,
                packId = route.packId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.EditCard>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.EditCard>()
            val vm: EditCardVM = hiltViewModel()
            EditCardScreen(
                vm = vm,
                packId = route.packId,
                cardId = route.cardId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.GameSetup>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.GameSetup>()
            val vm: GameSetupVM = hiltViewModel()
            GameSetupScreenStateful(
                onBackClick = { navController.popBackStack() },
                onStartGameClick = { navController.navigate(MyLibraryRoute.InGame(route.packId)) },
                vm = vm
            )
        }

        composable<MyLibraryRoute.InGame>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { backStackEntry ->
            val vm = navController.shareViewModel<CustomInGameVM>(backStackEntry)
            LaunchedEffect(Unit) {
                Log.d("MyLibraryNav", "CustomInGame: ${vm.hashCode()}")
            }
            val route = backStackEntry.toRoute<MyLibraryRoute.InGame>()
            vm.currentGameID = route.packId
            ActiveGameScreenStateful(
                onBackClick = { navController.popBackStack() },
                onExitGame = { navController.navigate(MyLibraryRoute.EndGame) },
                packId = route.packId,
                mViewModel = vm
            )
        }

        composable<MyLibraryRoute.EndGame>(
            enterTransition = { enter() }, exitTransition = { exit() },
            popEnterTransition = { popEnter() }, popExitTransition = { popExit() }
        ) { backStackEntry ->
            val vm = navController.shareViewModel<CustomInGameVM>(backStackEntry)
            LaunchedEffect(Unit) {
                Log.d("MyLibraryNav", "CustomEndGame: ${vm.hashCode()}")
            }
            GameEndScreen(
                onBackClick = {
                    navController.navigate(RootRoute.MyLibrary) {
                        popUpTo(RootRoute.MyLibrary)
                        launchSingleTop = true
                    }
                },
                onPlayAgainClick = {
                    vm.resetAllData()
                    navController.popBackStack<MyLibraryRoute.GameSetup>(inclusive = false)
                },
                vm = vm
            )
        }
    }
}
