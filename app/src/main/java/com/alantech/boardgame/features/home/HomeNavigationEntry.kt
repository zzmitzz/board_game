package com.alantech.boardgame.features.home

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.alantech.boardgame.features.gameend.GameEndScreen
import com.alantech.boardgame.features.gamesetup.GameSetupScreen
import com.alantech.boardgame.features.gamesetup.GameSetupScreenStateful
import com.alantech.boardgame.features.gamesetup.GameSetupVM
import com.alantech.boardgame.features.home.screen.HomeScreen
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.screen.ActiveGameScreenStateful
import com.alantech.boardgame.features.pagedetail.PageDetailScreen
import com.alantech.boardgame.navigation.RootRoute
import kotlinx.serialization.Serializable


sealed class HomeRoute {
    @Serializable
    data object Main : HomeRoute()
    @Serializable
    data class PackDetail(val id: String) : HomeRoute()
    @Serializable
    data class GameSetupLobby(val id: String) : HomeRoute()

    @Serializable
    data class InGame(val id: String)

    @Serializable
    data object EndGame

    @Serializable
    data object Setting : HomeRoute()
}

fun NavGraphBuilder.homeNavigationEntry(
    navController: NavHostController
) {
    navigation<RootRoute.Home>(
        startDestination = HomeRoute.Main
    ) {
        composable<HomeRoute.Main> {
            HomeScreen(
                {}, { packID ->
                    navController.navigate(HomeRoute.PackDetail(packID))
                }, {}
            )
        }
        composable<HomeRoute.PackDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.PackDetail>()
            PageDetailScreen(
                { navController.popBackStack() }, {}, {
                    navController.navigate(HomeRoute.GameSetupLobby(route.id))
                }
            )
        }
        composable<HomeRoute.GameSetupLobby> { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.GameSetupLobby>()
            val viewModel = hiltViewModel<GameSetupVM>()
            GameSetupScreenStateful(
                onBackClick = { navController.popBackStack() },
                onStartGameClick = { navController.navigate(HomeRoute.InGame(route.id)) },
                vm = viewModel
            )
        }

        composable<HomeRoute.InGame> { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.InGame>()
            val viewModel = hiltViewModel<InGameVM>()
            ActiveGameScreenStateful(
                onGameEnd = { navController.navigate(HomeRoute.EndGame) },
                onBackClick = { navController.popBackStack() },
                mViewModel = viewModel
            )
        }

        composable<HomeRoute.EndGame> {
            GameEndScreen(

            )
        }

    }
}