package com.alantech.boardgame.features.ingame.screen

import androidx.activity.ExperimentalActivityApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.UIEffect
import com.alantech.boardgame.features.ingame.UIState
import com.alantech.boardgame.features.ingame.components.ActionButtons
import com.alantech.boardgame.features.ingame.components.ActiveGameTopBar
import com.alantech.boardgame.features.ingame.components.CardEffectWrapper
import com.alantech.boardgame.features.ingame.components.ChallengeCard
import com.alantech.boardgame.features.ingame.components.InGameHeader
import com.alantech.boardgame.features.ingame.components.PlayerTurnChip
import com.alantech.boardgame.ui.app.LocalSnackbarHostState
import com.alantech.boardgame.ui.common.LoadingComponent
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.model.cardDetailPack
import com.alantech.boardgame.ui.theme.LightBackground


@Composable
fun ActiveGameScreenStateful(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onGameEnd: () -> Unit = {},
    mViewModel: InGameVM
){
    val toast = LocalSnackbarHostState.current
    val uiState = mViewModel.uiState.collectAsStateWithLifecycle()
    val activeGameContext = remember { ActiveGameScreenContractImpl(mViewModel) }

    val uiEffect = mViewModel.uiEffect.collectAsStateWithLifecycle(
        initialValue = null
    )

    LaunchedEffect(key1 = uiEffect.value) {
        when(val effect = uiEffect.value){
            is UIEffect.OnGameEnd -> {
                onGameEnd()
            }
            is UIEffect.ShowToast -> {
                toast.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }
            else -> {

            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when(val state = uiState.value){
            is UIState.DataLoading -> {
                LoadingComponent()
            }
            is UIState.DataError -> {

            }
            is UIState.InGameUIState -> {
                with(activeGameContext){
                    ActiveGameScreen(
                        onBackClick,
                        onSettingsClick,
                        state = state)
                }
            }
        }

    }
}


interface ActiveGameScreenContract{
    fun onComplete()
    fun onPauseClick()
    fun onForfeit()
}


context(actionContext : ActiveGameScreenContract)
@Composable
fun ActiveGameScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    state: UIState.InGameUIState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InGameHeader(
            gameName = state.gameTitle,
            roundText = "ROUND ${state.round}/10",
            onCloseClick = onBackClick,
            onPauseClick = {
                actionContext.onPauseClick()
            },
            onSettingsClick = onSettingsClick
        )
        Spacer(modifier = Modifier.height(16.dp))

        PlayerTurnChip(playerName = state.currentPlayer.name)
        
        Spacer(modifier = Modifier.height(24.dp))

        CardEffectWrapper(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            item = state.currentCard,
            penalty = state.penalty

        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            ActionButtons(
                onComplete = {
                    actionContext.onComplete()
                },
                onForfeit = { }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview
@Composable
private fun PVActiveGame() {
    val mock = object : ActiveGameScreenContract {
        override fun onPauseClick() {
        }

        override fun onComplete() {

        }

        override fun onForfeit() {
        }
    }
    with(mock){
        ActiveGameScreen(
            {},{},
            state = UIState.InGameUIState(0, 0,
                cardDetailPack[0], GamePlayer(1, Color(0xFF000000), "Player1"), "", "")
        )
    }
}