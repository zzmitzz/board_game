package com.alantech.boardgame.features.onboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.alantech.boardgame.features.onboard.screen.OnBoardScreen
import com.alantech.boardgame.features.onboard.screen.ScreenA
import com.alantech.boardgame.features.onboard.screen.ScreenB
import com.alantech.boardgame.features.onboard.screen.ScreenC
import com.alantech.boardgame.navigation.RootRoute
import kotlinx.serialization.Serializable


sealed class OnBoardRoute {
    @Serializable data object Screen1 : OnBoardRoute()
    @Serializable data object Screen2 : OnBoardRoute()
    @Serializable data object Screen3 : OnBoardRoute()
}

fun NavGraphBuilder.onBoardingGraph(
    navController: NavHostController,
) {
    navigation<RootRoute.Onboarding>(
        startDestination = OnBoardRoute.Screen1
    ) {
        composable<OnBoardRoute.Screen1> {
            OnBoardScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(OnBoardRoute.Screen2) }
            ) {
                ScreenA()
            }
        }
        composable<OnBoardRoute.Screen2> {
            OnBoardScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(OnBoardRoute.Screen3) }
            ) {
                ScreenB()
            }
        }
        composable<OnBoardRoute.Screen3> {
            OnBoardScreen(
                modifier = Modifier.fillMaxSize(),
                onBack = { navController.popBackStack() },
                onNext = { navController.popBackStack() }
            ) {
                ScreenC()
            }
        }
    }
}