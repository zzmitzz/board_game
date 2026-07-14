package com.alantech.boardgame.features.ingame.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alantech.boardgame.R
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.UIEffect
import com.alantech.boardgame.features.ingame.UIState
import com.alantech.boardgame.features.ingame.components.ActionButtons
import com.alantech.boardgame.features.ingame.components.CardEffectWrapper
import com.alantech.boardgame.features.ingame.components.InGameHeader
import com.alantech.boardgame.features.ingame.components.PlayerTurnChip
import com.alantech.boardgame.features.ingame.components.TinderCardStack
import com.alantech.boardgame.features.ingame.dialog.CardHintDialog
import com.alantech.boardgame.features.ingame.dialog.ConfirmExitDialog
import com.alantech.boardgame.features.ingame.dialog.SettingDialog
import com.alantech.boardgame.ui.app.LocalSnackbarHostState
import com.alantech.boardgame.ui.common.ErrorComponent
import com.alantech.boardgame.ui.common.LoadingComponent
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.CardDetailMedia
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.utils.BlurBackgroundDialog
import com.alantech.boardgame.utils.DialogListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ActiveGameScreenStateful(
    onExitGame: () -> Unit = {},
    packId: String = "",
    mViewModel: InGameVM,
) {
    val toast = LocalSnackbarHostState.current
    val uiState = mViewModel.uiState.collectAsStateWithLifecycle()
    val timeLeft = mViewModel.timeLeft.collectAsStateWithLifecycle()
    val gamePermissionSetting = mViewModel.gamePersistenceSetting.collectAsStateWithLifecycle()
    val activeGameContext = remember { ActiveGameScreenContractImpl(mViewModel) }
    val isCardHintOpen = remember { mutableStateOf(false) }
    val dialogOnExitGame = remember { mutableStateOf(false) }
    val dialogOnSetting = remember { mutableStateOf(false) }
    val onDialogListener = remember {
        object : DialogListener {
            override fun onConfirm(numberPlayer: Int) {
                mViewModel.onEndGame()
            }

            override fun onCancel() {
                dialogOnExitGame.value = false
                isCardHintOpen.value = false
                dialogOnSetting.value = false
            }

            override fun onDismiss() {
                dialogOnExitGame.value = false
                isCardHintOpen.value = false
                dialogOnSetting.value = false
            }

        }
    }
    val uiEffect = mViewModel.uiEffect.collectAsStateWithLifecycle(
        initialValue = null
    )

    LaunchedEffect(Unit) {
        mViewModel.initGame(packId)
    }

    LaunchedEffect(key1 = uiEffect.value) {
        when (val effect = uiEffect.value) {
            is UIEffect.OnGameEnd -> {
                onExitGame()
            }

            is UIEffect.ShowToast -> {
                toast.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Short
                )
            }

            is UIEffect.OnTimeUp -> {
                mViewModel.onCardComplete(false)
            }

            is UIEffect.OnGameError -> {
                toast.showSnackbar(
                    message = effect.message,
                    duration = SnackbarDuration.Long
                )
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState.value) {
            is UIState.DataLoading -> {
                LoadingComponent()
            }

            is UIState.DataError -> {
                ErrorComponent(message = state.message)
            }

            is UIState.InGameUIState -> {
                with(activeGameContext) {
                    ActiveGameScreen(
                        onBackClick = {
                            dialogOnExitGame.value = true
                        },
                        onSettingsClick = {
                            dialogOnSetting.value = true
                        },
                        onCardHintClick = {
                            isCardHintOpen.value = true
                        },
                        state = state,
                        timeLeft = if (timeLeft.value != 0) timeLeft.value.toString() else stringResource(
                            id = R.string.time_up
                        )
                    )
                }
            }
        }
    }
    AnimatedVisibility(visible = dialogOnExitGame.value) {
        BlurBackgroundDialog(
            listener = onDialogListener
        ) {
            ConfirmExitDialog(
                onConfirm = {
                    dialogOnExitGame.value = false
                    onDialogListener.onConfirm(-1)
                },
                onCancel = {
                    dialogOnExitGame.value = false
                    onDialogListener.onCancel()
                }
            )
        }
    }
    AnimatedVisibility(
        visible = dialogOnSetting.value
    ) {
        BlurBackgroundDialog(
            listener = onDialogListener
        ) {
            SettingDialog(
                initialSetting = gamePermissionSetting.value,
                onSave = {
                    dialogOnSetting.value = false
                    mViewModel.onSaveSetting(it)
                },
                onCancel = {
                    dialogOnSetting.value = false
                    onDialogListener.onCancel()
                }
            )
        }
    }

    AnimatedVisibility(
        visible = isCardHintOpen.value && (uiState.value is UIState.InGameUIState)
    ) {
        BlurBackgroundDialog(
            listener = onDialogListener
        ) {
            val currentCard = (uiState.value as UIState.InGameUIState).currentPackCards[
                    (uiState.value as UIState.InGameUIState).currentCardIndex
            ]
            CardHintDialog(
                cardHint = currentCard.hint
            )
        }
    }
}


interface ActiveGameScreenContract {
    fun onExitGameClick()
    fun onComplete()
    fun onForfeit()

}


context(actionContext: ActiveGameScreenContract)
@Composable
fun ActiveGameScreen(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCardHintClick: () -> Unit = {},
    state: UIState.InGameUIState,
    timeLeft: String = ""
) {

    var showShuffleCardsAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(state.round) {
        launch {
            showShuffleCardsAnimation = true
            delay(2000L)
            showShuffleCardsAnimation = false
        }
    }
    val lottieShuffle by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.lottie_shuffle_card)
    )
    val alphaVisible = animateFloatAsState(
        targetValue = if (showShuffleCardsAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400)
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InGameHeader(
            gameName = state.gameTitle,
            roundText = "${stringResource(R.string.round)} ${state.round}/${state.totalRound}",
            onCloseClick = onBackClick,
            onSettingsClick = onSettingsClick,
            timeLeft = timeLeft
        )
        Spacer(modifier = Modifier.height(16.dp))

        PlayerTurnChip(playerName = state.currentPlayer.name)

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LottieAnimation(
                composition = lottieShuffle,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .height(300.dp)
                    .graphicsLayer { alpha = alphaVisible.value }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - alphaVisible.value }
            ) {
                TinderCardStack(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    cards = state.currentPackCards,
                    currentIndex = state.currentCardIndex,
                    penalty = state.penalty,
                    onCardHintClick = onCardHintClick
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ActionButtons(
                        onComplete = {
                            actionContext.onComplete()
                        },
                        onForfeit = {
                            actionContext.onForfeit()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Preview
@Composable
private fun PVActiveGame() {
    val mock = object : ActiveGameScreenContract {

        override fun onExitGameClick() {

        }

        override fun onComplete() {

        }

        override fun onForfeit() {
        }
    }
    with(mock) {
        ActiveGameScreen(
            {}, {},
            state = UIState.InGameUIState(
                0, 0,
                0, mutableListOf<CardDetail>().apply {
                    add(CardDetail(
                        id = "",
                        category = "Dare",
                        description = "There is the card-th",
                        media = CardDetailMedia(
                            image = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
                            video = null
                        ), hint = ""

                    ))
                }, GamePlayer(1, Color(0xFF000000), "Player1"), "", "",
            )
        )
    }
}