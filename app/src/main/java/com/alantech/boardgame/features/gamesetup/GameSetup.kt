package com.alantech.boardgame.features.gamesetup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alantech.boardgame.R
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.features.gamesetup.components.AddPlayerDialog
import com.alantech.boardgame.features.gamesetup.components.GameSetupTopBar
import com.alantech.boardgame.features.gamesetup.components.HouseRulesSection
import com.alantech.boardgame.features.gamesetup.components.PlayerSection
import com.alantech.boardgame.features.ingame.dialog.SettingDialog
import com.alantech.boardgame.ui.app.LocalSnackbarHostState
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.utils.BlurBackgroundDialog
import com.alantech.boardgame.utils.DialogListener
import com.alantech.boardgame.utils.DialogPlayerListener
import kotlinx.coroutines.flow.collectLatest


@Composable
fun GameSetupScreenStateful(
    onBackClick: () -> Unit = {},
    onStartGameClick: () -> Unit = {},
    vm: GameSetupVM
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHostState.current
    var lastClickStart = remember { 0L }
    LaunchedEffect(vm.uiEffect) {
        vm.uiEffect.collectLatest { effect ->
            when(effect){
                is GameSetupUIEffect.onToast -> {
                    snackBarHost.showSnackbar(effect.message)
                }
                is GameSetupUIEffect.onGameStart -> {
                    onStartGameClick.invoke()
                }
            }
        }
    }

    val gameActionHandler = remember {
        object : GameSetupScreenAction {
            override fun onStartGame() {
                val now = System.currentTimeMillis()
                if (now - lastClickStart < 1000) return
                lastClickStart = now
                vm.saveGameConfigSession()
            }

            override fun onDeletePlayer(id: Int) {
                vm.removePlayer(id)
            }

            override fun onRemoveAllPlayers() {
                vm.removeAllPlayers()
            }

            override fun onAddPlayers(player: Int) {
                vm.addPlayer(player)
            }

            override fun onNameChange(id: Int, name: String) {
                vm.updatePlayerName(id, name)
            }

            override fun onRecordDaresChange(b: Boolean) {
                vm.setRecordDares(b)
            }

            override fun onNSFWContentChange(b: Boolean) {
                vm.setNsfwEnable(b)
            }

            override fun onSpeedModeChange(b: Boolean) {
                vm.setSpeedRun(b)
            }

            override fun onPenaltyChange(b: Boolean) {
                vm.setPenalty(b)
            }

            override fun onRoundChange(round: Int) {
                vm.setRoundAmount(round)
            }

            override fun onSaveSetting(setting: com.alantech.boardgame.config.PersistenceSetting) {
                vm.onSaveSetting(setting)
            }
        }
    }

    with(gameActionHandler) {
        GameSetupScreen(
            uiState = uiState,
            onBackClick = onBackClick,
        )
    }
}

interface GameSetupScreenAction {
    fun onStartGame()
    fun onDeletePlayer(id: Int)
    fun onRemoveAllPlayers()
    fun onAddPlayers(player: Int)
    fun onNameChange(id: Int, name: String)

    fun onRecordDaresChange(b: Boolean)
    fun onNSFWContentChange(b: Boolean)
    fun onSpeedModeChange(b: Boolean)
    fun onPenaltyChange(b: Boolean)
    fun onRoundChange(round: Int)
    fun onSaveSetting(setting: PersistenceSetting)
}


context(action: GameSetupScreenAction)
@Composable
fun GameSetupScreen(
    uiState: GameSetupUIState = GameSetupUIState(),
    onBackClick: () -> Unit = {},
) {

    var showAddingMemberDialog by remember { mutableStateOf(false) }
    var showSettingDialog by remember { mutableStateOf(false) }
    val dialogListener = remember {
        object : DialogPlayerListener() {
            override fun onConfirm(players: Int) {
                showAddingMemberDialog = false
                action.onAddPlayers(players)
            }

            override fun onCancel() {
                showAddingMemberDialog = false
                showSettingDialog = false
            }

            override fun onDismiss() {
                showAddingMemberDialog = false
                showSettingDialog = false
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF15101C))
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        GameSetupTopBar(
            onBackClick = onBackClick,
            onSettingClick = { showSettingDialog = true }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            PlayerSection(
                mPlayers = uiState.players,
                onAddPlayerClick = {
                    showAddingMemberDialog = !showAddingMemberDialog
                },
                onDeletePlayerClick = {
                    action.onDeletePlayer(it)
                },
                onDeleteAllPlayersClick = {
                    action.onRemoveAllPlayers()
                },
                onNameChange = { id, name ->
                    action.onNameChange(id, name)
                }
            )
            Spacer(modifier = Modifier.height(48.dp))
            HouseRulesSection(
                recordDares = uiState.isRecordDares,
                nsfwContent = uiState.nsfwEnable,
                speedMode = uiState.speedRun,
                penalty = uiState.penalty,
                round = uiState.roundAmount,
                onRecordDaresChange = { action.onRecordDaresChange(it) },
                onNsfwContentChange = { action.onNSFWContentChange(it) },
                onSpeedModeChange = { action.onSpeedModeChange(it) },
                onPenaltyChange = { action.onPenaltyChange(it) },
                onRoundChange = { action.onRoundChange(it) },
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { action.onStartGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC084FC)
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF4C1D95)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.start_game),
                color = Color(0xFF4C1D95),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    AnimatedVisibility(
        visible = showAddingMemberDialog,
    ) {
        AddPlayerDialog(dialogListener)
    }

    AnimatedVisibility(visible = showSettingDialog) {
        BlurBackgroundDialog(listener = dialogListener) {
            SettingDialog(
                initialSetting = uiState.setting,
                onSave = {
                    showSettingDialog = false
                    action.onSaveSetting(it)
                },
                onCancel = {
                    showSettingDialog = false
                    dialogListener.onCancel()
                }
            )
        }
    }

}

@Preview
@Composable
private fun GameSuPV() {
    val mock = object : GameSetupScreenAction{
        override fun onStartGame() {}
        override fun onDeletePlayer(id: Int) {}
        override fun onRemoveAllPlayers() {}
        override fun onAddPlayers(player: Int) {}
        override fun onNameChange(id: Int, name: String) {}
        override fun onRecordDaresChange(b: Boolean) {}
        override fun onNSFWContentChange(b: Boolean) {}
        override fun onSpeedModeChange(b: Boolean) {}
        override fun onPenaltyChange(b: Boolean) {}
        override fun onRoundChange(round: Int) {}
        override fun onSaveSetting(setting: PersistenceSetting) {}
    }
    with(mock){
        GameSetupScreen {  }
    }
}