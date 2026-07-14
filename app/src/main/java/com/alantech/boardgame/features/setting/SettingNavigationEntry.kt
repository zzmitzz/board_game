package com.alantech.boardgame.features.setting

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.alantech.boardgame.navigation.RootRoute
import kotlinx.serialization.Serializable


sealed class SettingRoute {
    @Serializable
    data object Setting : SettingRoute()


}

fun NavGraphBuilder.settingNavigationEntry(
    navController: NavController
) {
    navigation<RootRoute.Setting>(
        startDestination = SettingRoute.Setting){
        composable<SettingRoute.Setting>{
            SettingScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}