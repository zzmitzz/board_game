package com.boardgame.deepdeck.features.moments

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.entity.MomentType
import com.boardgame.deepdeck.features.ingame.InGameVM
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.findActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * In-game capture flow: the Photo / Video / Voice sheet ([showOptions]), the camera-app
 * launchers, the RECORD_AUDIO request and the voice recorder sheet. [onBusyChange] reports
 * when the camera app, the permission dialog or the recorder is up so the turn timer holds.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentCaptureFlow(
    vm: InGameVM,
    showOptions: Boolean,
    onDismissOptions: () -> Unit,
    onBusyChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val recordingSince by vm.recordingSince.collectAsStateWithLifecycle()

    var showRecorder by rememberSaveable { mutableStateOf(false) }
    /** The camera app or the mic permission dialog is in front of the game. */
    var systemUiOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showRecorder, systemUiOpen) { onBusyChange(showRecorder || systemUiOpen) }
    // Results arrive before ON_RESUME; this also unsticks the flag if a result never comes back.
    LifecycleResumeEffect(Unit) {
        systemUiOpen = false
        onPauseOrDispose { }
    }

    val showMessage: (Int) -> Unit = { res -> scope.launch { snackbar.showSnackbar(context.getString(res)) } }

    val onCameraResult: (Boolean) -> Unit = { ok ->
        systemUiOpen = false
        vm.finishCapture(success = ok)
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture(), onCameraResult)
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo(), onCameraResult)

    val launchCamera: (MomentType) -> Unit = { type ->
        // Held from the tap so the timer stays paused while the file is reserved (IO).
        systemUiOpen = true
        scope.launch {
            val uri = vm.beginCapture(type)
            if (uri == null) {
                systemUiOpen = false
                showMessage(R.string.moment_capture_failed)
                return@launch
            }
            try {
                if (type == MomentType.VIDEO) videoLauncher.launch(uri) else photoLauncher.launch(uri)
            } catch (_: ActivityNotFoundException) {
                systemUiOpen = false
                vm.finishCapture(success = false)
                showMessage(R.string.no_camera_app)
            }
        }
    }

    // The manifest declares CAMERA (live calls), so the camera-app intents need it granted too.
    var pendingCameraType by rememberSaveable { mutableStateOf<MomentType?>(null) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        systemUiOpen = false
        val type = pendingCameraType
        pendingCameraType = null
        if (granted && type != null) {
            launchCamera(type)
            return@rememberLauncherForActivityResult
        }
        val blocked = activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
        scope.launch {
            if (blocked) {
                val result = snackbar.showSnackbar(
                    message = context.getString(R.string.camera_permission_blocked),
                    actionLabel = context.getString(R.string.open_settings),
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) context.openAppDetailsSettings()
            } else {
                snackbar.showSnackbar(context.getString(R.string.camera_permission_needed))
            }
        }
    }
    val openCamera: (MomentType) -> Unit = { type ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera(type)
        } else {
            pendingCameraType = type
            systemUiOpen = true
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        systemUiOpen = false
        if (granted) {
            showRecorder = true
            return@rememberLauncherForActivityResult
        }
        // No rationale after a denial = "Don't ask again": only Settings can grant it now.
        val blocked = activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
        scope.launch {
            if (blocked) {
                val result = snackbar.showSnackbar(
                    message = context.getString(R.string.mic_permission_blocked),
                    actionLabel = context.getString(R.string.open_settings),
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) context.openAppDetailsSettings()
            } else {
                snackbar.showSnackbar(context.getString(R.string.mic_permission_needed))
            }
        }
    }
    val openRecorder = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            showRecorder = true
        } else {
            systemUiOpen = true
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // The recorder closes itself once a clip stops (saved, cancelled or hit the 2-minute cap).
    var wasRecording by remember { mutableStateOf(false) }
    LaunchedEffect(recordingSince) {
        if (recordingSince != null) {
            wasRecording = true
        } else if (wasRecording) {
            wasRecording = false
            showRecorder = false
        }
    }

    // Backgrounded mid-clip: keep what was recorded (the mic is cut off in the background anyway).
    // Leaving the game screen mid-clip discards it. Rotation keeps recording: the VM owns the recorder.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && activity?.isChangingConfigurations != true) {
                vm.stopVoice(save = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (activity?.isChangingConfigurations != true) vm.stopVoice(save = false)
        }
    }

    if (showOptions) {
        ModalBottomSheet(
            onDismissRequest = onDismissOptions,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DeepTalkTheme.colors.bgElevated,
        ) {
            CaptureOptionsContent(
                onPick = { type ->
                    onDismissOptions()
                    if (type == MomentType.AUDIO) openRecorder() else openCamera(type)
                }
            )
        }
    }

    if (showRecorder) {
        val recording = recordingSince != null
        ModalBottomSheet(
            // Back / scrim while recording = cancel (the clip is discarded, like the Cancel button).
            onDismissRequest = {
                vm.stopVoice(save = false)
                showRecorder = false
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            sheetGesturesEnabled = !recording,
            containerColor = DeepTalkTheme.colors.bgElevated,
        ) {
            VoiceRecorderContent(
                recordingSince = recordingSince,
                onStart = { vm.startVoice() },
                onStop = { vm.stopVoice(save = true) },
                onCancel = {
                    vm.stopVoice(save = false)
                    showRecorder = false
                },
            )
        }
    }
}

@Composable
private fun CaptureOptionsContent(onPick: (MomentType) -> Unit) {
    val colors = DeepTalkTheme.colors
    Column(Modifier.padding(start = Spacing.gutter, end = Spacing.gutter, bottom = Spacing.xl)) {
        Text(stringResource(R.string.capture_moment), style = DeepTalkTheme.type.headline, color = colors.textPrimary)
        Text(
            stringResource(R.string.capture_moment_sheet_desc),
            style = DeepTalkTheme.type.label,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = Spacing.md)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            CaptureOption(MomentType.PHOTO, R.string.moment_photo_desc, Modifier.weight(1f)) { onPick(MomentType.PHOTO) }
            CaptureOption(MomentType.VIDEO, R.string.moment_video_desc, Modifier.weight(1f)) { onPick(MomentType.VIDEO) }
            CaptureOption(MomentType.AUDIO, R.string.moment_voice_desc, Modifier.weight(1f)) { onPick(MomentType.AUDIO) }
        }
    }
}

@Composable
private fun CaptureOption(type: MomentType, description: Int, modifier: Modifier, onClick: () -> Unit) {
    val colors = DeepTalkTheme.colors
    val accent = type.accent()
    Column(
        modifier
            .clip(DeepTalkShapes.md)
            .background(accent.copy(alpha = 0.1f))
            .border(1.dp, accent.copy(alpha = 0.3f), DeepTalkShapes.md)
            .pressable(pressedScale = 0.95f, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(48.dp)
                .background(accent.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(type.icon, contentDescription = null, tint = accent)
        }
        Spacer(Modifier.height(10.dp))
        Text(stringResource(type.label), style = DeepTalkTheme.type.title, color = colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(
            stringResource(description),
            style = DeepTalkTheme.type.label,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VoiceRecorderContent(
    recordingSince: Long?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val recording = recordingSince != null
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(recordingSince) {
        while (recordingSince != null) {
            now = SystemClock.elapsedRealtime()
            delay(200)
        }
    }
    val elapsed = recordingSince?.let { (now - it).coerceIn(0, MAX_VOICE_MS) } ?: 0L
    val dotAlpha = if (recording && !DeepTalkTheme.reducedMotion) {
        rememberInfiniteTransition(label = "recDot").animateFloat(
            initialValue = 1f,
            targetValue = 0.25f,
            animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
            label = "recDotAlpha"
        ).value
    } else 1f

    // Landscape / short screens: mic beside the timer and the two buttons side by side, so
    // Record / Stop & save and Cancel stay reachable; the column also scrolls as a last resort.
    val compact = LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP
    val primaryButton: @Composable (Modifier) -> Unit = { modifier ->
        PrimaryButton(
            text = stringResource(if (recording) R.string.voice_stop_save else R.string.voice_record),
            onClick = if (recording) onStop else onStart,
            leadingIcon = if (recording) Icons.Rounded.Stop else Icons.Rounded.Mic,
            height = if (compact) 48.dp else 56.dp,
            modifier = modifier
        )
    }
    val cancelButton: @Composable (Modifier) -> Unit = { modifier ->
        GhostButton(
            text = stringResource(R.string.cancel_text),
            onClick = onCancel,
            leadingIcon = Icons.Rounded.Close,
            modifier = modifier
        )
    }
    val timer: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (recording) {
                Box(
                    Modifier
                        .size(10.dp)
                        .alpha(dotAlpha)
                        .background(colors.rose, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(formatClipDuration(elapsed), style = DeepTalkTheme.type.display, color = colors.textPrimary)
        }
        Text(
            if (recording) stringResource(R.string.voice_recording)
            else stringResource(R.string.voice_ready),
            style = DeepTalkTheme.type.label,
            color = colors.textSecondary
        )
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = Spacing.gutter, end = Spacing.gutter, bottom = if (compact) Spacing.md else Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.voice_clip_title), style = DeepTalkTheme.type.headline, color = colors.textPrimary)
        if (compact) {
            Spacer(Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                MicDisc(recording, size = 56.dp)
                Spacer(Modifier.width(Spacing.md))
                Column { timer() }
            }
        } else {
            Spacer(Modifier.height(Spacing.lg))
            MicDisc(recording, size = 96.dp)
            Spacer(Modifier.height(Spacing.md))
            timer()
        }
        Spacer(Modifier.height(Spacing.sm))
        // Progress towards the 2-minute cap.
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(DeepTalkShapes.pill)
                .background(colors.surfaceHigh)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(elapsed / MAX_VOICE_MS.toFloat())
                    .height(4.dp)
                    .background(colors.mint)
            )
        }
        Text(
            stringResource(R.string.voice_max_length, formatClipDuration(MAX_VOICE_MS)),
            style = DeepTalkTheme.type.label,
            color = colors.textMuted,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
        if (compact) {
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                cancelButton(Modifier.weight(1f))
                primaryButton(Modifier.weight(1.4f))
            }
        } else {
            Spacer(Modifier.height(Spacing.lg))
            primaryButton(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.sm))
            cancelButton(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MicDisc(recording: Boolean, size: Dp) {
    val colors = DeepTalkTheme.colors
    Box(
        Modifier
            .size(size)
            .glow(colors.mint, radius = 28.dp, alpha = if (recording) 0.35f else 0.12f, offsetY = 0.dp)
            .background(colors.mint.copy(alpha = 0.14f), CircleShape)
            .border(1.5.dp, colors.mint.copy(alpha = if (recording) 0.8f else 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.Mic, contentDescription = null, tint = colors.mint, modifier = Modifier.size(size * 0.42f))
    }
}

/** Below this screen height (dp) the recorder switches to its compact layout. */
private const val COMPACT_HEIGHT_DP = 520

/** App info screen (permissions) for a permanently denied runtime permission. */
private fun Context.openAppDetailsSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
