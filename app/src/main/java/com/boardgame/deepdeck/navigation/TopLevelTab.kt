package com.boardgame.deepdeck.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Style
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.home.HomeRoute

/** Bottom navigation: exactly three tabs (plan §3.1). */
enum class TopLevelTab(
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    PLAY(R.string.tab_play, Icons.Outlined.Style, Icons.Rounded.Style),
    LIBRARY(R.string.tab_library, Icons.Outlined.BookmarkBorder, Icons.Rounded.Bookmark),
    YOU(R.string.tab_you, Icons.Outlined.PersonOutline, Icons.Rounded.Person);

    companion object {
        /** The tab whose root is [destination], or null (bottom bar hidden). */
        fun rootOf(destination: NavDestination?): TopLevelTab? = when {
            destination == null -> null
            destination.hasRoute<HomeRoute.Main>() -> PLAY
            destination.hasRoute<RootRoute.Library>() -> LIBRARY
            destination.hasRoute<RootRoute.You>() -> YOU
            else -> null
        }
    }
}

/** Tab switch: Play pops back to Home; the others sit on top of Home with saved state. */
fun NavController.navigateToTab(tab: TopLevelTab) {
    when (tab) {
        TopLevelTab.PLAY -> {
            if (!popBackStack<HomeRoute.Main>(inclusive = false, saveState = true)) {
                navigate(RootRoute.Home) { launchSingleTop = true }
            }
        }
        TopLevelTab.LIBRARY, TopLevelTab.YOU -> {
            val route: Any = if (tab == TopLevelTab.LIBRARY) RootRoute.Library else RootRoute.You
            navigate(route) {
                popUpTo<HomeRoute.Main> { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}
