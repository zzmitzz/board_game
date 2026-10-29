package com.boardgame.deepdeck.features.call.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.SignalWifiStatusbarConnectedNoInternet4
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.call.CallEffect
import com.boardgame.deepdeck.features.call.CallPhase
import com.boardgame.deepdeck.features.call.CallUiState
import com.boardgame.deepdeck.features.call.CallVM
import com.boardgame.deepdeck.features.call.EndReason
import com.boardgame.deepdeck.features.call.callInviteLink
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.shareText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import org.webrtc.VideoTrack

private val CALL_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

/** Lobby (preview + name) → the call (video, controls, chat) → the ended screen. */
@Composable
fun CallScreen(vm: CallVM, onExit: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val local by vm.localVideo.collectAsStateWithLifecycle()
    val remote by vm.remoteVideo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val haptics = LocalDeepTalkHaptics.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var asked by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.onPermissions(mic = context.granted(Manifest.permission.RECORD_AUDIO), camera = context.granted(Manifest.permission.CAMERA))
    }
    val requestPermissions = {
        // A second ask after "Don't ask again" shows nothing: send the user to Settings instead.
        if (asked && CALL_PERMISSIONS.none { context.granted(it) }) context.openAppSettings()
        else permissionLauncher.launch(CALL_PERMISSIONS)
        asked = true
    }
    LaunchedEffect(Unit) {
        val mic = context.granted(Manifest.permission.RECORD_AUDIO)
        val camera = context.granted(Manifest.permission.CAMERA)
        if ((mic && camera) || asked) vm.onPermissions(mic, camera) else requestPermissions()
    }

    LifecycleResumeEffect(Unit) {
        vm.onScreenVisible(true)
        onPauseOrDispose { vm.onScreenVisible(false) }
    }

    // The screen must not sleep mid-call.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    var incoming by remember { mutableStateOf<CallEffect.IncomingMessage?>(null) }
    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is CallEffect.PeerJoined -> {
                    haptics.confirm()
                    snackbar.showSnackbar(context.getString(R.string.call_peer_joined, effect.name))
                }
                is CallEffect.PeerLeft -> snackbar.showSnackbar(
                    context.getString(if (effect.timedOut) R.string.call_peer_timed_out else R.string.call_peer_left, effect.name)
                )
                is CallEffect.IncomingMessage -> {
                    haptics.press()
                    incoming = effect
                }
            }
        }
    }
    LaunchedEffect(incoming) {
        if (incoming != null) {
            delay(4_000)
            incoming = null
        }
    }

    var confirmLeave by remember { mutableStateOf(false) }
    BackHandler {
        when {
            state.chatOpen -> vm.setChatOpen(false)
            state.inRoom -> confirmLeave = true
            state.phase == CallPhase.JOINING -> vm.leave()
            else -> onExit()
        }
    }

    val copyCode = {
        context.copyToClipboard(state.code)
        scope.launch { snackbar.showSnackbar(context.getString(R.string.call_invite_copied)) }
        Unit
    }
    val shareInvite = {
        context.shareText(
            context.getString(R.string.call_invite_text, state.code, callInviteLink(state.code)),
            chooserTitle = context.getString(R.string.call_share_title)
        )
    }

    when (state.phase) {
        CallPhase.LOBBY, CallPhase.JOINING -> LobbyContent(
            state = state,
            local = local,
            eglContext = vm.eglContext,
            onBack = onExit,
            onNameChange = vm::setDisplayName,
            onToggleMic = vm::toggleMic,
            onToggleCamera = vm::toggleCamera,
            onSwitchCamera = vm::switchCamera,
            onRequestPermissions = requestPermissions,
            onCopy = copyCode,
            onShare = shareInvite,
            onJoin = vm::join,
        )
        CallPhase.WAITING, CallPhase.CONNECTING, CallPhase.IN_CALL -> RoomContent(
            state = state,
            local = local,
            remote = remote,
            eglContext = vm.eglContext,
            incoming = incoming,
            onDismissIncoming = { incoming = null },
            onToggleMic = vm::toggleMic,
            onToggleCamera = vm::toggleCamera,
            onSwitchCamera = vm::switchCamera,
            onToggleSpeaker = vm::toggleSpeaker,
            onChatOpen = vm::setChatOpen,
            onSendChat = vm::sendChat,
            onChatInput = vm::onChatInputChanged,
            onRetryChat = vm::retryMessage,
            onLeave = { confirmLeave = true },
            onCopy = copyCode,
            onShare = shareInvite,
        )
        CallPhase.ENDED -> EndedContent(state = state, onBackHome = onExit, onRetry = vm::retry)
    }

    if (confirmLeave) {
        LeaveDialog(
            onConfirm = {
                confirmLeave = false
                vm.leave()
            },
            onDismiss = { confirmLeave = false }
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Lobby
// ---------------------------------------------------------------------------------------------

@Composable
private fun LobbyContent(
    state: CallUiState,
    local: VideoTrack?,
    eglContext: EglBase.Context,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onRequestPermissions: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onJoin: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val joining = state.phase == CallPhase.JOINING
    Box(Modifier.fillMaxSize().background(colors.bgBase)) {
        SelfView(state, local, eglContext, Modifier.fillMaxSize())
        Scrims()

        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), onBack)
            Spacer(Modifier.weight(1f))
            StatusPill(stringResource(R.string.call_room_label, state.code))
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            if (!state.hasMicPermission || !state.hasCameraPermission) {
                PermissionCard(onRequestPermissions)
            }
            if (state.isHost) {
                InviteCard(
                    code = state.code,
                    title = stringResource(R.string.call_share_title),
                    description = stringResource(R.string.call_waiting_desc),
                    onCopy = onCopy,
                    onShare = onShare
                )
            }
            GlassSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.md)) {
                    Text(stringResource(R.string.call_your_name).uppercase(), style = DeepTalkTheme.type.overline, color = colors.textMuted)
                    Spacer(Modifier.height(6.dp))
                    BasicTextField(
                        value = state.displayName,
                        onValueChange = onNameChange,
                        singleLine = true,
                        enabled = !joining,
                        textStyle = DeepTalkTheme.type.title.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.brand),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .background(colors.bgBase.copy(alpha = 0.45f), DeepTalkShapes.sm)
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                if (state.displayName.isEmpty()) {
                                    Text(stringResource(R.string.call_you), style = DeepTalkTheme.type.title, color = colors.textMuted)
                                }
                                inner()
                            }
                        }
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        MicButton(state, onToggleMic)
                        CameraButton(state, onToggleCamera)
                        CallControlButton(
                            icon = Icons.Rounded.Cameraswitch,
                            contentDescription = stringResource(R.string.call_switch_camera),
                            onClick = onSwitchCamera,
                            enabled = state.hasCameraPermission && state.cameraOn,
                            size = 48.dp
                        )
                        PrimaryButton(
                            text = stringResource(R.string.call_join_call),
                            onClick = onJoin,
                            loading = joining,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(onRequest: () -> Unit) {
    val colors = DeepTalkTheme.colors
    GlassSurface(Modifier.fillMaxWidth(), tint = colors.gold) {
        Column(Modifier.padding(Spacing.md)) {
            Text(stringResource(R.string.call_permissions_title), style = DeepTalkTheme.type.title, color = colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.call_permissions_desc), style = DeepTalkTheme.type.body, color = colors.textSecondary)
            Spacer(Modifier.height(Spacing.sm))
            GhostButton(text = stringResource(R.string.call_permissions_allow), onClick = onRequest)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// In the room
// ---------------------------------------------------------------------------------------------

@Composable
private fun RoomContent(
    state: CallUiState,
    local: VideoTrack?,
    remote: VideoTrack?,
    eglContext: EglBase.Context,
    incoming: CallEffect.IncomingMessage?,
    onDismissIncoming: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onChatOpen: (Boolean) -> Unit,
    onSendChat: (String) -> Unit,
    onChatInput: (String) -> Unit,
    onRetryChat: (String) -> Unit,
    onLeave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val peer = state.peer
    // Controls fade out after a few seconds of a running call; a tap brings them back.
    var controlsVisible by remember { mutableStateOf(true) }
    var touches by remember { mutableIntStateOf(0) }
    val autoHide = state.phase == CallPhase.IN_CALL && !state.chatOpen
    LaunchedEffect(controlsVisible, autoHide, touches) {
        if (controlsVisible && autoHide) {
            delay(5_000)
            controlsVisible = false
        }
    }
    val showControls = controlsVisible || !autoHide
    val touched: (() -> Unit) -> () -> Unit = { action -> { touches++; action() } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                controlsVisible = !controlsVisible
            }
    ) {
        // Main stage: the other person, or yourself while waiting.
        when {
            peer == null -> SelfView(state, local, eglContext, Modifier.fillMaxSize())
            peer.cameraOn && remote != null -> VideoRenderer(remote, eglContext, Modifier.fillMaxSize())
            else -> AvatarBackdrop(
                name = peer.name,
                subtitle = stringResource(R.string.call_camera_is_off),
                modifier = Modifier.fillMaxSize()
            )
        }
        Scrims()

        // A SurfaceView punches through anything drawn over it, so the self view starts low (clear of
        // the banners) and hides while the chat sheet is up.
        if (peer != null && !state.chatOpen) {
            DraggablePip(Modifier.statusBarsPadding().navigationBarsPadding(), topInset = 168.dp, bottomInset = 168.dp) {
                SelfView(state, local, eglContext, Modifier.fillMaxSize(), overlay = true, compact = true)
            }
        }

        // Top bar + banners.
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            AnimatedVisibility(showControls, enter = fadeIn(), exit = fadeOut()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CallStatusPill(state)
                    Spacer(Modifier.weight(1f))
                    if (state.hasCameraPermission && state.cameraOn) {
                        GlassIconButton(Icons.Rounded.Cameraswitch, stringResource(R.string.call_switch_camera), touched(onSwitchCamera))
                    }
                }
            }
            when {
                state.reconnecting -> CallBanner(stringResource(R.string.call_you_reconnecting), Icons.Rounded.CloudOff)
                peer?.reconnecting == true -> CallBanner(stringResource(R.string.call_peer_reconnecting, peer.name), Icons.Rounded.SignalWifiStatusbarConnectedNoInternet4)
                state.mediaReconnecting -> CallBanner(stringResource(R.string.call_media_reconnecting), Icons.Rounded.WifiTethering)
                state.connectTrouble -> CallBanner(stringResource(R.string.call_connect_trouble), Icons.Rounded.WifiTethering, tint = colors.rose)
            }
            AnimatedVisibility(incoming != null && !state.chatOpen, enter = slideInVertically() + fadeIn(), exit = fadeOut()) {
                incoming?.let { message ->
                    GlassSurface(
                        Modifier.fillMaxWidth().clickable {
                            onDismissIncoming()
                            onChatOpen(true)
                        }
                    ) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(message.name, style = DeepTalkTheme.type.overline, color = colors.brand)
                            Text(message.text, style = DeepTalkTheme.type.body, color = colors.textPrimary, maxLines = 2)
                        }
                    }
                }
            }
        }

        if (peer == null && !state.chatOpen) {
            InviteCard(
                code = state.code,
                title = stringResource(R.string.call_waiting_title),
                description = stringResource(R.string.call_waiting_desc),
                onCopy = onCopy,
                onShare = onShare,
                waiting = true,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = Spacing.gutter)
            )
        }

        // Name tag + controls.
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(bottom = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (peer != null && showControls) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(peer.name)
                    if (!peer.micOn) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            Modifier.size(30.dp).background(colors.rose, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.MicOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            AnimatedVisibility(
                showControls && !state.chatOpen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Row(
                    Modifier
                        .padding(horizontal = Spacing.md)
                        .background(Color.Black.copy(alpha = 0.45f), DeepTalkShapes.pill)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MicButton(state, touched(onToggleMic))
                    CameraButton(state, touched(onToggleCamera))
                    CallControlButton(
                        icon = if (state.speakerOn) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = stringResource(R.string.call_speaker),
                        onClick = touched(onToggleSpeaker),
                        off = !state.speakerOn
                    )
                    CallControlButton(
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = stringResource(R.string.call_chat),
                        onClick = touched { onChatOpen(true) },
                        badge = state.unread
                    )
                    CallControlButton(
                        icon = Icons.Rounded.CallEnd,
                        contentDescription = stringResource(R.string.call_leave),
                        onClick = onLeave,
                        danger = true
                    )
                }
            }
        }

        AnimatedVisibility(
            state.chatOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            ChatPanel(
                messages = state.messages,
                peerName = peer?.name,
                peerTyping = state.peerTyping,
                onSend = onSendChat,
                onInputChange = onChatInput,
                onRetry = onRetryChat,
                onClose = { onChatOpen(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.62f)
                    // Swallow taps so they don't toggle the controls underneath.
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            )
        }
    }
}

@Composable
private fun CallStatusPill(state: CallUiState) {
    val colors = DeepTalkTheme.colors
    when (state.phase) {
        CallPhase.IN_CALL -> {
            val startedAt = state.callStartedAt ?: return
            var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
            LaunchedEffect(startedAt) {
                while (true) {
                    now = SystemClock.elapsedRealtime()
                    delay(1_000)
                }
            }
            StatusPill(formatDuration(now - startedAt), dot = colors.mint)
        }
        CallPhase.CONNECTING -> StatusPill(stringResource(R.string.call_connecting), dot = colors.gold)
        else -> StatusPill(stringResource(R.string.call_room_label, state.code), dot = colors.mint)
    }
}

// ---------------------------------------------------------------------------------------------
// Ended
// ---------------------------------------------------------------------------------------------

@Composable
private fun EndedContent(state: CallUiState, onBackHome: () -> Unit, onRetry: () -> Unit) {
    val colors = DeepTalkTheme.colors
    val reason = state.endReason ?: EndReason.LEFT
    val message = when (reason) {
        EndReason.LEFT -> R.string.call_ended_left
        EndReason.ROOM_NOT_FOUND -> R.string.call_ended_not_found
        EndReason.ROOM_FULL -> R.string.call_ended_full
        EndReason.REPLACED -> R.string.call_ended_replaced
        EndReason.UNAUTHORIZED -> R.string.call_ended_unauthorized
        EndReason.CONNECTION_LOST -> R.string.call_ended_lost
    }
    GlowBackground(accent = if (reason == EndReason.LEFT) colors.brand else colors.ember) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(Spacing.gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(72.dp).background(Brush.linearGradient(listOf(colors.surfaceHigh, colors.surface)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CallEnd, contentDescription = null, tint = colors.rose, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(stringResource(R.string.call_ended_title), style = DeepTalkTheme.type.hero, color = colors.textPrimary, textAlign = TextAlign.Center)
            state.endedCallMillis?.let {
                Spacer(Modifier.height(4.dp))
                Text(formatDuration(it), style = DeepTalkTheme.type.title, color = colors.textSecondary)
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(stringResource(message), style = DeepTalkTheme.type.body, color = colors.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Spacing.xxl))
            PrimaryButton(text = stringResource(R.string.call_back_home), onClick = onBackHome, modifier = Modifier.fillMaxWidth())
            if (reason == EndReason.CONNECTION_LOST) {
                Spacer(Modifier.height(Spacing.sm))
                GhostButton(text = stringResource(R.string.call_try_again), onClick = onRetry, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LeaveDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = DeepTalkTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceHigh,
        icon = { Icon(Icons.Rounded.CallEnd, contentDescription = null, tint = colors.rose) },
        title = { Text(stringResource(R.string.call_leave_title), style = DeepTalkTheme.type.headline, color = colors.textPrimary) },
        text = { Text(stringResource(R.string.call_leave_desc), style = DeepTalkTheme.type.body, color = colors.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.call_leave), color = colors.rose) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.call_stay), color = colors.brand) }
        }
    )
}

// ---------------------------------------------------------------------------------------------
// Pieces
// ---------------------------------------------------------------------------------------------

/** Your own camera (mirrored on the front camera, like a mirror), or your initial when it is off. */
@Composable
private fun SelfView(
    state: CallUiState,
    local: VideoTrack?,
    eglContext: EglBase.Context,
    modifier: Modifier,
    overlay: Boolean = false,
    compact: Boolean = false,
) {
    if (local != null && state.cameraOn) {
        VideoRenderer(local, eglContext, modifier, mirror = state.frontCamera, overlay = overlay)
    } else {
        AvatarBackdrop(
            name = state.displayName.ifBlank { stringResource(R.string.call_you) },
            subtitle = stringResource(R.string.call_camera_is_off),
            modifier = modifier,
            compact = compact
        )
    }
}

/** Dark gradients at the top and bottom so white controls read on any video. */
@Composable
private fun Scrims() {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().height(160.dp)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)))
        )
        Box(
            Modifier.fillMaxWidth().height(220.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))))
        )
    }
}

@Composable
private fun MicButton(state: CallUiState, onClick: () -> Unit) {
    val on = state.hasMicPermission && state.micOn
    CallControlButton(
        icon = if (on) Icons.Rounded.Mic else Icons.Rounded.MicOff,
        contentDescription = stringResource(if (on) R.string.call_mute else R.string.call_unmute),
        onClick = onClick,
        off = !on,
        enabled = state.hasMicPermission,
        size = if (state.inRoom) 56.dp else 48.dp
    )
}

@Composable
private fun CameraButton(state: CallUiState, onClick: () -> Unit) {
    val on = state.hasCameraPermission && state.cameraOn
    CallControlButton(
        icon = if (on) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
        contentDescription = stringResource(if (on) R.string.call_camera_off else R.string.call_camera_on),
        onClick = onClick,
        off = !on,
        enabled = state.hasCameraPermission,
        size = if (state.inRoom) 56.dp else 48.dp
    )
}

private fun formatDuration(millis: Long): String {
    val total = (millis / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun Context.granted(permission: String) =
    androidx.core.content.ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Context.copyToClipboard(text: String) {
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("DeepTalk room", text))
}

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
