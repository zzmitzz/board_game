package com.alantech.boardgame.features.ingame.screen

import android.app.Dialog
import androidx.activity.ExperimentalActivityApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.features.home.HomeRoute
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.UIEffect
import com.alantech.boardgame.features.ingame.UIState
import com.alantech.boardgame.features.ingame.components.ActionButtons
import com.alantech.boardgame.features.ingame.components.ActiveGameTopBar
import com.alantech.boardgame.features.ingame.components.CardEffectWrapper
import com.alantech.boardgame.features.ingame.components.ChallengeCard
import com.alantech.boardgame.features.ingame.components.InGameHeader
import com.alantech.boardgame.features.ingame.components.PlayerTurnChip
import com.alantech.boardgame.features.ingame.dialog.CardHintDialog
import com.alantech.boardgame.features.ingame.dialog.ConfirmExitDialog
import com.alantech.boardgame.features.ingame.dialog.SettingDialog
import com.alantech.boardgame.ui.app.LocalSnackbarHostState
import com.alantech.boardgame.ui.common.LoadingComponent
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.model.cardDetailPack
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.utils.BlurBackgroundDialog
import com.alantech.boardgame.utils.DialogListener


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

            else -> {

            }
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
                        timeLeft = timeLeft.value
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
            CardHintDialog(
                cardHint = (uiState.value as (UIState.InGameUIState)).currentCard.hint
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
    timeLeft: Int = 30

) {
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
            roundText = "ROUND ${state.round}/${state.totalRound}",
            onCloseClick = onBackClick,
            onSettingsClick = onSettingsClick,
            timeLeft = timeLeft
        )
        Spacer(modifier = Modifier.height(16.dp))

        PlayerTurnChip(playerName = state.currentPlayer.name)

        Spacer(modifier = Modifier.height(24.dp))

        CardEffectWrapper(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            item = state.currentCard,
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
                cardDetailPack[0], GamePlayer(1, Color(0xFF000000), "Player1"), "", "",
            )
        )
    }
}