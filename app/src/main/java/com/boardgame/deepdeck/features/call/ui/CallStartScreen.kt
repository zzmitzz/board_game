package com.boardgame.deepdeck.features.call.ui

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.repository.RoomProblem
import com.boardgame.deepdeck.features.call.CallStartEffect
import com.boardgame.deepdeck.features.call.CallStartUiState
import com.boardgame.deepdeck.features.call.CallStartVM
import com.boardgame.deepdeck.features.call.ROOM_CODE_LENGTH
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing

/** "Talk live": start a room (get a code to share) or type the code a friend sent. */
@Composable
fun CallStartScreen(
    vm: CallStartVM,
    onBackClick: () -> Unit,
    onRoomReady: (code: String, host: Boolean) -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val haptics = LocalDeepTalkHaptics.current

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is CallStartEffect.OpenRoom -> {
                    haptics.confirm()
                    onRoomReady(effect.code, effect.host)
                }
                is CallStartEffect.Problem -> snackbar.showSnackbar(context.getString(effect.problem.message()))
            }
        }
    }

    CallStartContent(
        state = state,
        onBackClick = onBackClick,
        onCreateRoom = vm::createRoom,
        onCodeChange = vm::onCodeChange,
        onPaste = { vm.pasteCode(context.clipboardText()) },
        onJoin = vm::joinRoom,
    )
}

@Composable
private fun CallStartContent(
    state: CallStartUiState,
    onBackClick: () -> Unit,
    onCreateRoom: () -> Unit,
    onCodeChange: (String) -> Unit,
    onPaste: () -> Unit,
    onJoin: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    GlowBackground(accent = colors.rose.copy(alpha = 0.8f)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.gutter)
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                onClick = onBackClick,
                modifier = Modifier.padding(top = Spacing.md)
            )
            Spacer(Modifier.height(Spacing.xl))
            Box(
                Modifier
                    .size(64.dp)
                    .glow(colors.rose, radius = 28.dp, alpha = 0.35f)
                    .background(Brush.linearGradient(listOf(colors.rose, colors.brandStrong)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(stringResource(R.string.call_entry_title), style = DeepTalkTheme.type.hero, color = colors.textPrimary)
            Spacer(Modifier.height(Spacing.xs))
            Text(stringResource(R.string.call_start_subtitle), style = DeepTalkTheme.type.body, color = colors.textSecondary)
            Spacer(Modifier.height(Spacing.xxl))

            GlassSurface(Modifier.fillMaxWidth().staggeredEntrance(0)) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.call_start_room), style = DeepTalkTheme.type.headline, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.call_start_room_desc), style = DeepTalkTheme.type.body, color = colors.textSecondary)
                    Spacer(Modifier.height(Spacing.md))
                    PrimaryButton(
                        text = stringResource(R.string.call_start_room),
                        onClick = onCreateRoom,
                        leadingIcon = Icons.Rounded.VideoCall,
                        loading = state.creating,
                        enabled = !state.checking,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(Modifier.weight(1f), color = colors.outline)
                Text(
                    stringResource(R.string.call_join_divider).uppercase(),
                    style = DeepTalkTheme.type.overline,
                    color = colors.textMuted,
                    modifier = Modifier.padding(horizontal = Spacing.sm)
                )
                HorizontalDivider(Modifier.weight(1f), color = colors.outline)
            }

            GlassSurface(Modifier.fillMaxWidth().staggeredEntrance(1)) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.call_code_label).uppercase(),
                        style = DeepTalkTheme.type.overline,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    RoomCodeField(code = state.code, onCodeChange = onCodeChange, onDone = onJoin)
                    Spacer(Modifier.height(Spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        GhostButton(
                            text = stringResource(R.string.call_paste),
                            onClick = onPaste,
                            leadingIcon = Icons.Rounded.ContentPaste,
                            height = 56.dp
                        )
                        PrimaryButton(
                            text = stringResource(R.string.call_join),
                            onClick = onJoin,
                            leadingIcon = Icons.AutoMirrored.Rounded.Login,
                            enabled = state.canJoin,
                            loading = state.checking,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

/** Six boxes over one hidden text field: typing, paste and backspace all behave as usual. */
@Composable
private fun RoomCodeField(code: String, onCodeChange: (String) -> Unit, onDone: () -> Unit) {
    val colors = DeepTalkTheme.colors
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Go,
            autoCorrectEnabled = false,
        ),
        keyboardActions = KeyboardActions(onGo = { onDone() }),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box {
                // Keeps the field focusable/editable; the boxes below are what people see.
                Box(Modifier.size(1.dp)) { inner() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(ROOM_CODE_LENGTH) { index ->
                        val char = code.getOrNull(index)
                        val active = index == code.length
                        Box(
                            Modifier
                                .weight(1f)
                                .height(56.dp)
                                .background(colors.bgBase.copy(alpha = 0.45f), DeepTalkShapes.sm)
                                .border(
                                    width = if (active) 2.dp else 1.dp,
                                    color = if (active) colors.brand else colors.outline,
                                    shape = DeepTalkShapes.sm
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char?.toString().orEmpty(),
                                style = DeepTalkTheme.type.display,
                                color = colors.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    )
}

private fun RoomProblem.message(): Int = when (this) {
    RoomProblem.NOT_FOUND -> R.string.call_problem_not_found
    RoomProblem.FULL -> R.string.call_problem_full
    RoomProblem.OFFLINE -> R.string.call_problem_offline
    RoomProblem.SERVER -> R.string.call_problem_server
}

private fun Context.clipboardText(): String? =
    getSystemService(ClipboardManager::class.java)?.primaryClip
        ?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)
        ?.coerceToText(this)
        ?.toString()
