package com.boardgame.deepdeck.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.boardgame.deepdeck.features.home.homeNavigationEntry
import com.boardgame.deepdeck.features.ingame.gameFlowNavigationEntry
import com.boardgame.deepdeck.features.language.languageNavigationEntry
import com.boardgame.deepdeck.features.library.libraryTabEntry
import com.boardgame.deepdeck.features.mylibrary.myLibraryNavigationEntry
import com.boardgame.deepdeck.features.you.youTabEntry
import com.boardgame.deepdeck.ui.components.LocalSharedTransitionScope
import com.boardgame.deepdeck.ui.theme.LocalReducedMotion
import com.boardgame.deepdeck.ui.theme.Motion

val LocalNavController = compositionLocalOf<NavHostController?> {
    null
}

/**
 * Root NavHost inside a SharedTransitionLayout (pack cover shared elements).
 * One motion spec for every route: shared axis X; fade-through between tabs
 * and under reduced motion.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val offsetPx = with(LocalDensity.current) { Motion.SharedAxisOffset.roundToPx() }
    val reduced = LocalReducedMotion.current
    SharedTransitionLayout(modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = RootRoute.Home,
                enterTransition = {
                    val tabSwitch = TopLevelTab.rootOf(initialState.destination) != null &&
                        TopLevelTab.rootOf(targetState.destination) != null
                    if (reduced || tabSwitch) Motion.fadeThroughEnter()
                    else Motion.sharedAxisEnter(offsetPx, forward = true)
                },
                exitTransition = {
                    val tabSwitch = TopLevelTab.rootOf(initialState.destination) != null &&
                        TopLevelTab.rootOf(targetState.destination) != null
                    if (reduced || tabSwitch) Motion.fadeThroughExit()
                    else Motion.sharedAxisExit(offsetPx, forward = true)
                },
                popEnterTransition = {
                    val tabSwitch = TopLevelTab.rootOf(initialState.destination) != null &&
                        TopLevelTab.rootOf(targetState.destination) != null
                    if (reduced || tabSwitch) Motion.fadeThroughEnter()
                    else Motion.sharedAxisEnter(offsetPx, forward = false)
                },
                popExitTransition = {
                    val tabSwitch = TopLevelTab.rootOf(initialState.destination) != null &&
                        TopLevelTab.rootOf(targetState.destination) != null
                    if (reduced || tabSwitch) Motion.fadeThroughExit()
                    else Motion.sharedAxisExit(offsetPx, forward = false)
                }
            ) {
                homeNavigationEntry(navController)
                libraryTabEntry(navController)
                youTabEntry(navController)
                languageNavigationEntry(navController)
                myLibraryNavigationEntry(navController)
                gameFlowNavigationEntry(navController)
            }
        }
    }
}
