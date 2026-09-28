package com.boardgame.deepdeck.features.home

import androidx.compose.ui.graphics.toArgb
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.tileImage
import com.boardgame.deepdeck.features.home.screen.HomeScreen
import com.boardgame.deepdeck.features.home.screen.HomeScreenVM
import com.boardgame.deepdeck.features.home.section_detail.SectionDetailScreens
import com.boardgame.deepdeck.features.home.vibe.VibeScreen
import com.boardgame.deepdeck.features.ingame.startGame
import com.boardgame.deepdeck.features.mylibrary.MyLibraryRoute
import com.boardgame.deepdeck.features.pagedetail.PageDetailScreen
import com.boardgame.deepdeck.features.search.GameSearchStateful
import com.boardgame.deepdeck.navigation.RootRoute
import com.boardgame.deepdeck.navigation.TopLevelTab
import com.boardgame.deepdeck.navigation.navigateToTab
import com.boardgame.deepdeck.ui.components.ProvideNavAnimatedScope
import kotlinx.serialization.Serializable


sealed class HomeRoute {
    @Serializable
    data object Main : HomeRoute()

    /**
     * [coverKey]: shared-element key of the tapped tile's cover (null = no shared transition).
     * [coverUrl]/[title]/[accent] let the hero render instantly while details load.
     */
    @Serializable
    data class PackDetail(
        val id: String,
        val coverKey: String? = null,
        val coverUrl: String? = null,
        val title: String? = null,
        val accent: String? = null,
    ) : HomeRoute()

    @Serializable
    data object GameSearch

    @Serializable
    data class SectionDetail(val sectionID: String, val title: String? = null) : HomeRoute()

    /** Vibe screen; display data is passed along so the hero renders instantly. */
    @Serializable
    data class Vibe(
        val vibeId: String,
        val name: String,
        val iconKey: String,
        val colorStart: Int,
        val colorEnd: Int,
        val description: String? = null,
    ) : HomeRoute()
}

fun NavGraphBuilder.homeNavigationEntry(
    navController: NavHostController
) {
    navigation<RootRoute.Home>(
        startDestination = HomeRoute.Main
    ) {
        composable<HomeRoute.Main> {
            ProvideNavAnimatedScope(this) {
                val mViewModel = hiltViewModel<HomeScreenVM>()
                HomeScreen(
                    viewModel = mViewModel,
                    onPackClick = { pack, coverKey -> navController.openPack(pack, coverKey) },
                    onSearchClick = { navController.navigate(HomeRoute.GameSearch) },
                    onSeeAllClick = { id, title -> navController.navigate(HomeRoute.SectionDetail(id, title)) },
                    onVibeClick = { vibe ->
                        navController.navigate(
                            HomeRoute.Vibe(
                                vibeId = vibe.id,
                                name = vibe.name,
                                iconKey = vibe.iconKey,
                                colorStart = vibe.colorStart.toArgb(),
                                colorEnd = vibe.colorEnd.toArgb(),
                                description = vibe.description
                            )
                        )
                    },
                    onStreakClick = { navController.navigateToTab(TopLevelTab.YOU) },
                    onPlayPack = { packId -> navController.startGame(packId) },
                    onQuickPlay = { packId, isCustom ->
                        navController.startGame(packId, isCustom = isCustom, quickPlay = true)
                    },
                    onMakeDeckClick = { navController.navigate(MyLibraryRoute.AddPack) },
                )
            }
        }

        composable<HomeRoute.GameSearch> {
            GameSearchStateful(
                onBackClick = { navController.popBackStack() },
                onPackClick = { packId -> navController.navigate(HomeRoute.PackDetail(packId)) }
            )
        }

        composable<HomeRoute.Vibe> { backStackEntry ->
            ProvideNavAnimatedScope(this) {
                val route = backStackEntry.toRoute<HomeRoute.Vibe>()
                VibeScreen(
                    route = route,
                    onBackClick = { navController.popBackStack() },
                    onPackClick = { pack, coverKey -> navController.openPack(pack, coverKey) }
                )
            }
        }

        composable<HomeRoute.PackDetail> { backStackEntry ->
            ProvideNavAnimatedScope(this) {
                val route = backStackEntry.toRoute<HomeRoute.PackDetail>()
                PageDetailScreen(
                    route = route,
                    onBackClick = { navController.popBackStack() },
                    onPlayClick = { navController.startGame(route.id) }
                )
            }
        }

        composable<HomeRoute.SectionDetail> { backStackEntry ->
            ProvideNavAnimatedScope(this) {
                val route = backStackEntry.toRoute<HomeRoute.SectionDetail>()
                SectionDetailScreens(
                    initialTitle = route.title,
                    onBackClick = { navController.popBackStack() },
                    onCardClick = { pack, coverKey -> navController.openPack(pack, coverKey) }
                )
            }
        }
    }
}

/** Opens Pack detail with the data needed for an instant shared-element hero. */
fun NavController.openPack(pack: PacksPreview, coverKey: String? = null) {
    val id = pack.id ?: return
    navigate(
        HomeRoute.PackDetail(
            id = id,
            coverKey = coverKey,
            coverUrl = pack.tileImage,
            title = pack.title,
            accent = pack.accentColor
        )
    )
}
