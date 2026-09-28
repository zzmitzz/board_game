package com.boardgame.deepdeck.features.mylibrary

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.boardgame.deepdeck.features.ingame.startGame
import com.boardgame.deepdeck.features.library.LibraryTabScreen
import com.boardgame.deepdeck.navigation.RootRoute
import kotlinx.serialization.Serializable

sealed class MyLibraryRoute {
    @Serializable data object Library : MyLibraryRoute()
    @Serializable data object AddPack : MyLibraryRoute()
    @Serializable data class PackCards(val packId: String) : MyLibraryRoute()
    @Serializable data class AddCard(val packId: String) : MyLibraryRoute()
    @Serializable data class EditPack(val packId: String) : MyLibraryRoute()
    @Serializable data class EditCard(val packId: String, val cardId: String) : MyLibraryRoute()
}


fun NavGraphBuilder.myLibraryNavigationEntry(navController: NavController) {
    navigation<RootRoute.MyLibrary>(startDestination = MyLibraryRoute.Library) {

        // Legacy entry point of this graph: the Library tab UI, opened on "My packs".
        composable<MyLibraryRoute.Library> {
            LibraryTabScreen(
                onAddPackClick = { navController.navigate(MyLibraryRoute.AddPack) },
                onOpenPack = { packId -> navController.navigate(MyLibraryRoute.PackCards(packId)) },
                onEditPack = { packId -> navController.navigate(MyLibraryRoute.EditPack(packId)) },
                onPlayPack = { packId, isCustom -> navController.startGame(packId, isCustom = isCustom, quickPlay = true) },
                initialSegment = 1,
            )
        }

        composable<MyLibraryRoute.AddPack> {
            val vm: AddPackVM = hiltViewModel()
            AddPackScreen(
                vm = vm,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.PackCards> { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.PackCards>()
            val vm: PackCardsVM = hiltViewModel()
            PackCardsScreen(
                vm = vm,
                onBackClick = { navController.popBackStack() },
                onAddCardClick = { navController.navigate(MyLibraryRoute.AddCard(route.packId)) },
                onEditCardClick = { cardId -> navController.navigate(MyLibraryRoute.EditCard(route.packId, cardId)) },
                onPlayClick = { navController.startGame(route.packId, isCustom = true) }
            )
        }

        composable<MyLibraryRoute.AddCard> { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.AddCard>()
            val vm: AddCardVM = hiltViewModel()
            AddCardScreen(
                vm = vm,
                packId = route.packId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.EditPack> { backStackEntry ->
            val route = backStackEntry.toRoute<MyLibraryRoute.EditPack>()
            val vm: EditPackVM = hiltViewModel()
            EditPackScreen(
                vm = vm,
                packId = route.packId,
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<MyLibraryRoute.EditCard> { backStackEntry ->
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
    }
}
