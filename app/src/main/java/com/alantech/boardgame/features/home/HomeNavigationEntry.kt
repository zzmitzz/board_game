package com.alantech.boardgame.features.home

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import coil.util.Logger
import com.alantech.boardgame.features.gameend.GameEndScreen
import com.alantech.boardgame.features.gamesetup.GameSetupScreen
import com.alantech.boardgame.features.gamesetup.GameSetupScreenStateful
import com.alantech.boardgame.features.gamesetup.GameSetupVM
import com.alantech.boardgame.features.home.screen.HomeScreen
import com.alantech.boardgame.features.home.screen.HomeScreenVM
import com.alantech.boardgame.features.home.section_detail.SectionDetailScreens
import com.alantech.boardgame.features.home.shareviewmodel.shareViewModel
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.screen.ActiveGameScreenStateful
import com.alantech.boardgame.features.pagedetail.PageDetailScreen
import com.alantech.boardgame.features.search.GameSearchStateful
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
    data object GameSearch

    @Serializable
    data object Setting : HomeRoute()

    @Serializable
    data class SectionDetail(val sectionID: String) : HomeRoute()
}

fun NavGraphBuilder.homeNavigationEntry(
    navController: NavHostController
) {
    navigation<RootRoute.Home>(
        startDestination = HomeRoute.Main
    ) {
        composable<HomeRoute.Main>(
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
            val mViewModel = hiltViewModel<HomeScreenVM>()
            HomeScreen(
                {
                    navController.navigate(RootRoute.Setting)
                }, { packID ->
                    navController.navigate(HomeRoute.PackDetail(packID))
                }, {
                    navController.navigate(HomeRoute.GameSearch)
                },
                onSeeAllClick = {
                    navController.navigate(HomeRoute.SectionDetail(it))
                },
                viewModel = mViewModel
            )
        }

        composable<HomeRoute.GameSearch>(
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
            GameSearchStateful(
                onBackClick = { navController.popBackStack() },
                onPackClick = { packId -> navController.navigate(HomeRoute.PackDetail(packId)) }
            )
        }
        composable<HomeRoute.PackDetail>(
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
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.PackDetail>()
            PageDetailScreen(
                { navController.popBackStack() }, {}, {
                    navController.navigate(HomeRoute.GameSetupLobby(route.id))
                }
            )
        }
        composable<HomeRoute.GameSetupLobby>(
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
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.GameSetupLobby>()
            val viewModel = hiltViewModel<GameSetupVM>()
            GameSetupScreenStateful(
                onBackClick = { navController.popBackStack() },
                onStartGameClick = { navController.navigate(HomeRoute.InGame(route.id)) },
                vm = viewModel
            )
        }

        composable<HomeRoute.InGame>(
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { backStackEntry ->
            val viewModel = navController.shareViewModel<InGameVM>(backStackEntry)
            LaunchedEffect(Unit) {
                Log.d("HomeNavigationEntry", "InGame: ${viewModel.hashCode()}")
            }
            val id = backStackEntry.toRoute<HomeRoute.InGame>().id
            viewModel.currentGameID = id
            ActiveGameScreenStateful(
                onBackClick = { navController.popBackStack() },
                onExitGame = { navController.navigate(HomeRoute.EndGame) },
                packId = id,
                mViewModel = viewModel
            )
        }

        composable<HomeRoute.EndGame>(
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
        ) { backStackEntry ->
            val viewModel = navController.shareViewModel<InGameVM>(backStackEntry)
            LaunchedEffect(Unit) {
                Log.d("HomeNavigationEntry", "EndGame: ${viewModel.hashCode()}")
            }
            GameEndScreen(
                onBackClick = {
                    navController.navigate(
                        RootRoute.Home
                    ){
                        popUpTo(RootRoute.Home)
                        launchSingleTop = true
                    }
                },
                onPlayAgainClick = {
                    viewModel.resetAllData()
                    navController.popBackStack<HomeRoute.GameSetupLobby>(inclusive = false)
                },
                vm = viewModel
            )
        }

        composable<HomeRoute.SectionDetail>(
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
        ) { backStackEntry ->
            SectionDetailScreens(
                onBackClick = { navController.popBackStack() },
                onCardClick = { navController.navigate(HomeRoute.PackDetail(it)) }
            )
        }

    }
}