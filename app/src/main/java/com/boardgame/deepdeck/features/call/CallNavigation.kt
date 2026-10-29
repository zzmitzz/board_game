package com.boardgame.deepdeck.features.call

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.boardgame.deepdeck.features.call.ui.CallScreen
import com.boardgame.deepdeck.features.call.ui.CallStartScreen
import kotlinx.serialization.Serializable

/** Invite links: deepdeck://call/<CODE> (SplashScreenActivity forwards them to MainActivity). */
const val CALL_DEEP_LINK_BASE = "deepdeck://call"

fun callInviteLink(code: String) = "$CALL_DEEP_LINK_BASE/$code"

sealed class CallRoute {
    /** Start a room or type a friend's code. */
    @Serializable data object Start : CallRoute()

    /** Lobby → call for one room. [host] = this phone created the room (shows the invite first). */
    @Serializable data class Room(val code: String, val host: Boolean = false) : CallRoute()
}

fun NavGraphBuilder.callNavigationEntry(navController: NavController) {
    composable<CallRoute.Start> {
        CallStartScreen(
            vm = hiltViewModel(),
            onBackClick = { navController.popBackStack() },
            onRoomReady = { code, host ->
                navController.navigate(CallRoute.Room(code, host)) {
                    popUpTo<CallRoute.Start> { inclusive = true }
                }
            }
        )
    }
    composable<CallRoute.Room>(
        deepLinks = listOf(navDeepLink<CallRoute.Room>(basePath = CALL_DEEP_LINK_BASE))
    ) {
        CallScreen(
            vm = hiltViewModel(),
            onExit = { navController.popBackStack() }
        )
    }
}
