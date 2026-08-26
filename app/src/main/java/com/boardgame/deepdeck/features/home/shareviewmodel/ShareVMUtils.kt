package com.boardgame.deepdeck.features.home.shareviewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.boardgame.deepdeck.navigation.RootRoute


@Composable
inline fun <reified VM : ViewModel> NavController.shareViewModel(
    navBackStack: NavBackStackEntry
): VM {
    val graphEntry = remember(navBackStack) {
        getBackStackEntry<RootRoute.Home>()
    }
    return hiltViewModel<VM>(graphEntry)
}