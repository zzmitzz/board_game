package com.alantech.boardgame.features.ingame.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alantech.boardgame.R
import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.UIEffect
import com.alantech.boardgame.features.ingame.UIState
import com.alantech.boardgame.features.ingame.components.ActionButtons
import com.alantech.boardgame.features.ingame.components.EdgeToEdgeProgressBar
import com.alantech.boardgame.features.ingame.components.InGameHeader
import com.alantech.boardgame.features.ingame.components.InGameSettingDialog
import com.alantech.boardgame.features.ingame.components.PlayerTurnChip
import com.alantech.boardgame.features.ingame.components.TinderCardStack
import com.alantech.boardgame.features.ingame.dialog.CardHintDialog
import com.alantech.boardgame.features.ingame.dialog.ConfirmExitDialog
import com.alantech.boardgame.ui.app.LocalSnackbarHostState
import com.alantech.boardgame.ui.common.ErrorComponent
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.CardDetailMedia
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.utils.BlurBackgroundDialog
import com.alantech.boardgame.utils.DialogPlayerListener
import com.alantech.boardgame.utils.PlusJakartaSans
import com.alantech.boardgame.utils.SettingDialogListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun ActiveGameScreenStateful(
    onBackClick: () -> Unit = {},
    onExitGame: () -> Unit = {},
    packId: String = "",
    mViewModel: InGameVM,
) {


    val toast = LocalSnackbarHostState.current
    val uiState = mViewModel.uiState.collectAsStateWithLifecycle()
    val timeLeft = mViewModel.timeLeft.collectAsStateWithLifecycle()
    val activeGameContext = remember { ActiveGameScreenContractImpl(mViewModel) }
    val isCardHintOpen = remember { mutableStateOf(false) }
    val dialogOnExitGame = remember { mutableStateOf(false) }
    val lottieShuffle by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.lottie_shuffle_card)
    )
    var settingDialog by remember { mutableStateOf(false) }
    mViewModel.triggerGameFlow?.collectAsStateWithLifecycle()

    var isShuffling by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                }

                Lifecycle.Event.ON_STOP -> {
                    mViewModel.pauseTimer()
                }

                Lifecycle.Event.ON_START -> {
                }

                Lifecycle.Event.ON_RESUME -> {
                    mViewModel.resumeTimer()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val onDialogListener = remember {
        object : DialogPlayerListener() {
            override fun onConfirm(numberPlayer: Int) {
                mViewModel.onEndGame(true)
            }

            override fun onCancel() {
                dialogOnExitGame.value = false
                isCardHintOpen.value = false
            }

            override fun onDismiss() {
                dialogOnExitGame.value = false
                isCardHintOpen.value = false
            }

        }
    }

    val onDialogSettingListener = remember {
        object : SettingDialogListener() {
            override fun onConfirm(hapticEnabled: Boolean, soundEnabled: Boolean) {
                settingDialog = false
                mViewModel.saveSetting(hapticEnabled, soundEnabled)
            }

            override fun onCancel() {
                settingDialog = false
            }

            override fun onDismiss() {
                settingDialog = false
            }


        }
    }
    LaunchedEffect("initGame") {
        mViewModel.initGame(packId)
    }

    LaunchedEffect("observeEffect") {
        mViewModel.uiEffect.collect { effect ->
            when (effect) {
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
                    Log.i("DEBUG", "On time up: next")
                    mViewModel.onCardComplete(false)
                }

                is UIEffect.OnGameError -> {
                    toast.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Long
                    )
                }

                is UIEffect.OnShuffleCardsBegin -> {
                    isShuffling = true
                }

                is UIEffect.OnShuffleCardsEnd -> {
                    isShuffling = false
                }


                else -> {}
            }
        }
    }

    var mTextLoading by remember { mutableStateOf("") }

    LaunchedEffect(uiState.value) {
        if (uiState.value == UIState.DataLoading) {
            delay(500)
            mTextLoading = "Loading..."
            delay(3000)
            mTextLoading = "Translating Cards..."
            delay(3000)
            mTextLoading = "Shuffling Cards..."
        }
    }

    BackHandler(
        enabled = true
    ) {
        if(uiState.value is UIState.InGameUIState || uiState.value is UIState.DataLoading){
            dialogOnExitGame.value = true
        }else{
            onBackClick()
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        composition = lottieShuffle,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp)
                            .height(300.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(mTextLoading.isNotBlank()) {
                        Text(
                            text = mTextLoading,
                            fontSize = 20.sp,
                            fontFamily = PlusJakartaSans,
                            color = Color.White
                        )
                    }
                }
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
                            settingDialog = true
                        },
                        onCardHintClick = {
                            isCardHintOpen.value = true
                        },
                        state = state,
                        showShuffleCardsAnimation = isShuffling,
                        timeLeft = timeLeft.value
                    )
                }
                AnimatedVisibility(
                    visible = settingDialog
                ) {
                    BlurBackgroundDialog(
                        listener = onDialogSettingListener
                    ) {
                        InGameSettingDialog(
                            hapticEnabled = state.persistenceSetting.isHapticOn,
                            soundEnabled = state.persistenceSetting.isSoundOn,
                            onSave = { haptic,sound -> onDialogSettingListener.onConfirm(haptic, sound)},
                            onCancel = {
                                onDialogSettingListener.onCancel()
                            }
                        )
                    }
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
    showShuffleCardsAnimation: Boolean = false,
    timeLeft: Float = 0f
) {

    val onCardComplete = {
        if (!showShuffleCardsAnimation) {
            actionContext.onComplete()
        }
    }
    val onCardForfeit = {
        if (!showShuffleCardsAnimation) {
            actionContext.onForfeit()
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
                EdgeToEdgeProgressBar(
                    isEnable = GameSettingConfigCurrentSession.getIsTimerOn(),
                    progress = timeLeft / 30f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    ropeThickness = 4.dp,
                    ropeColor = LightSecondTextOBG,
                    trackColor = Color(0xFF333333),
                    cardBackgroundColor = Color.Transparent,
                    cornerRadius = 24.dp
                ) {
                    TinderCardStack(
                        modifier = Modifier
                            .fillMaxSize(),
                        cards = state.currentPackCards,
                        currentIndex = state.currentCardIndex,
                        penalty = state.penalty,
                        onCardHintClick = onCardHintClick
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    ActionButtons(
                        onComplete = onCardComplete,
                        onForfeit = onCardForfeit
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
                    add(
                        CardDetail(
                            id = "",
                            category = "Dare",
                            description = "There is the card-th",
                            media = CardDetailMedia(
                                image = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
                                video = null
                            ), hint = ""

                        )
                    )
                }, GamePlayer(1, Color(0xFF000000), "Player1"), "", "", PersistenceSetting()
            )
        )
    }
}