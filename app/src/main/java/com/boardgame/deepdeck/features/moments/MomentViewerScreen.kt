package com.boardgame.deepdeck.features.moments

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.entity.MomentEntity
import com.boardgame.deepdeck.data.local.entity.MomentType
import com.boardgame.deepdeck.data.repository.MomentRepository
import com.boardgame.deepdeck.ui.components.ErrorState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.OverlinePill
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.shareFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import javax.inject.Inject

sealed interface MomentViewerUiState {
    data object Loading : MomentViewerUiState
    /** The row no longer exists (deleted elsewhere). */
    data object Gone : MomentViewerUiState
    data class Ready(val moment: MomentEntity, val file: File, val fileExists: Boolean) : MomentViewerUiState
}

sealed interface MomentViewerUiEffect {
    data object Deleted : MomentViewerUiEffect
    data class Share(val uri: Uri, val mimeType: String) : MomentViewerUiEffect
}

@HiltViewModel
class MomentViewerVM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MomentRepository,
) : ViewModel() {

    private val momentId = savedStateHandle.toRoute<MomentsRoute.Viewer>().momentId

    val uiState: StateFlow<MomentViewerUiState> = repository.observe(momentId)
        .map { moment ->
            if (moment == null) return@map MomentViewerUiState.Gone
            val file = repository.fileOf(moment)
            MomentViewerUiState.Ready(moment, file, file.isFile && file.length() > 0)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MomentViewerUiState.Loading)

    private val _uiEffect = MutableSharedFlow<MomentViewerUiEffect>(extraBufferCapacity = 2)
    val uiEffect: SharedFlow<MomentViewerUiEffect> = _uiEffect.asSharedFlow()

    /** Removes the file and the row, then the screen closes. */
    fun delete() {
        val moment = (uiState.value as? MomentViewerUiState.Ready)?.moment ?: return
        viewModelScope.launch {
            repository.delete(moment)
            _uiEffect.emit(MomentViewerUiEffect.Deleted)
        }
    }

    fun share() {
        val ready = uiState.value as? MomentViewerUiState.Ready ?: return
        if (!ready.fileExists) return
        viewModelScope.launch {
            val uri = withContext(Dispatchers.IO) { runCatching { repository.uriOf(ready.moment.filePath) } }
                .onFailure { Log.w("MomentViewerVM", "share failed: ${it.message}") }
                .getOrNull() ?: return@launch
            _uiEffect.emit(MomentViewerUiEffect.Share(uri, ready.moment.momentType.mimeType))
        }
    }

    /** The player measured the real clip length: store it so tiles and viewer agree. */
    fun syncDuration(durationMs: Long) {
        viewModelScope.launch { repository.updateDuration(momentId, durationMs) }
    }
}

/**
 * Full-screen moment: photo (Coil), video (VideoView + MediaController) or voice clip
 * (MediaPlayer), with its card / player / date. Share via FileProvider, delete with confirm.
 * A missing or unreadable file shows an error state instead of crashing.
 */
@Composable
fun MomentViewerScreen(
    vm: MomentViewerVM,
    onBackClick: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = DeepTalkTheme.colors
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val latestOnBack by rememberUpdatedState(onBackClick)

    LaunchedEffect(vm) {
        vm.uiEffect.collect { effect ->
            when (effect) {
                MomentViewerUiEffect.Deleted -> latestOnBack()
                is MomentViewerUiEffect.Share ->
                    context.shareFile(effect.uri, effect.mimeType, chooserTitle = context.getString(R.string.share_moment))
            }
        }
    }

    val ready = state as? MomentViewerUiState.Ready
    val accent = ready?.moment?.momentType?.accent() ?: colors.brand

    GlowBackground(accent = accent.copy(alpha = 0.7f)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), onBackClick)
                Spacer(Modifier.weight(1f))
                if (ready != null) {
                    if (ready.fileExists) {
                        GlassIconButton(Icons.Rounded.IosShare, stringResource(R.string.share_moment), vm::share)
                    }
                    GlassIconButton(
                        Icons.Rounded.DeleteOutline,
                        stringResource(R.string.delete_moment),
                        { confirmDelete = true },
                        tint = colors.rose
                    )
                }
            }

            when (val current = state) {
                MomentViewerUiState.Loading -> Spacer(Modifier.weight(1f))
                MomentViewerUiState.Gone -> MissingMoment(Modifier.weight(1f))
                is MomentViewerUiState.Ready -> BoxWithConstraints(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val areaWidth = maxWidth
                    val areaHeight = maxHeight
                    // Landscape: media | info side by side so neither squeezes the other.
                    if (areaWidth > areaHeight) {
                        Row(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            MomentMedia(current, vm::syncDuration, Modifier.weight(1f).fillMaxHeight())
                            MomentInfo(
                                current.moment,
                                Modifier
                                    .width((areaWidth * 0.38f).coerceAtMost(380.dp))
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            MomentMedia(
                                current,
                                vm::syncDuration,
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.gutter, vertical = Spacing.sm)
                            )
                            // Capped so long card text scrolls instead of eating the media area.
                            MomentInfo(
                                current.moment,
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = areaHeight * 0.4f)
                                    .padding(horizontal = Spacing.gutter, vertical = Spacing.sm)
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = colors.surfaceHigh,
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = colors.rose) },
            title = { Text(stringResource(R.string.moment_delete_title), style = DeepTalkTheme.type.headline, color = colors.textPrimary) },
            text = { Text(stringResource(R.string.moment_delete_message), style = DeepTalkTheme.type.body, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.delete()
                }) { Text(stringResource(R.string.delete), color = colors.rose) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel_text), color = colors.brand) }
            }
        )
    }
}

/** Photo / video / voice player for a ready moment, with the missing / unplayable fallbacks. */
@Composable
private fun MomentMedia(
    current: MomentViewerUiState.Ready,
    onDurationKnown: (Long) -> Unit,
    modifier: Modifier,
) {
    var playbackFailed by remember(current.file) { mutableStateOf(false) }
    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            !current.fileExists -> MissingMoment()
            playbackFailed -> ErrorState(
                title = stringResource(R.string.moment_missing_title),
                message = stringResource(R.string.moment_playback_error),
                icon = Icons.Rounded.BrokenImage
            )
            else -> when (current.moment.momentType) {
                MomentType.PHOTO -> AsyncImage(
                    model = current.file,
                    contentDescription = current.moment.cardText,
                    contentScale = ContentScale.Fit,
                    onError = { playbackFailed = true },
                    modifier = Modifier.fillMaxSize()
                )
                MomentType.VIDEO -> VideoPlayer(current.file, onError = { playbackFailed = true })
                MomentType.AUDIO -> AudioPlayer(
                    file = current.file,
                    storedDurationMs = current.moment.durationMs,
                    onDurationKnown = onDurationKnown,
                    onError = { playbackFailed = true }
                )
            }
        }
    }
}

@Composable
private fun MissingMoment(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ErrorState(
            title = stringResource(R.string.moment_missing_title),
            message = stringResource(R.string.moment_missing_message),
            icon = Icons.Rounded.BrokenImage
        )
    }
}

/** Card / player / pack / date of the moment. */
@Composable
private fun MomentInfo(moment: MomentEntity, modifier: Modifier) {
    val colors = DeepTalkTheme.colors
    val context = LocalContext.current
    val type = moment.momentType
    GlassSurface(modifier, tint = type.accent()) {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OverlinePill(stringResource(type.label), type.accent(), icon = type.icon)
                Spacer(Modifier.width(8.dp))
                Text(formatMomentDate(context, moment.createdAt), style = DeepTalkTheme.type.label, color = colors.textMuted)
            }
            moment.playerName?.let {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.player_turn_format, it), style = DeepTalkTheme.type.title, color = colors.textPrimary)
            }
            moment.cardText?.let {
                Spacer(Modifier.height(4.dp))
                Text("“$it”", style = DeepTalkTheme.type.body, color = colors.textSecondary, maxLines = 4)
            }
            moment.packTitle?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = DeepTalkTheme.type.label, color = colors.textMuted, maxLines = 1)
            }
        }
    }
}

/** Platform VideoView (no extra player dependency); the MediaController shows on tap. */
@Composable
private fun VideoPlayer(file: File, onError: () -> Unit) {
    val latestOnError by rememberUpdatedState(onError)
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                val controller = MediaController(ctx)
                controller.setAnchorView(this)
                setMediaController(controller)
                tag = controller
                setOnPreparedListener { start() }
                setOnErrorListener { _, _, _ ->
                    latestOnError()
                    true
                }
                setVideoPath(file.absolutePath)
            }
        },
        onRelease = { view ->
            (view.tag as? MediaController)?.hide()
            view.stopPlayback()
        },
        modifier = Modifier.wrapContentSize()
    )
}

/**
 * Voice clip: MediaPlayer with play / pause and a seekable progress bar; released on dispose.
 * Compact (smaller disc / button) when the area is short, and scrolls as a last resort.
 * Reports the real file length via [onDurationKnown] when it disagrees with the stored one.
 */
@Composable
private fun AudioPlayer(
    file: File,
    storedDurationMs: Long?,
    onDurationKnown: (Long) -> Unit,
    onError: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val latestOnError by rememberUpdatedState(onError)
    val latestOnDuration by rememberUpdatedState(onDurationKnown)
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(storedDurationMs?.toInt() ?: 0) }
    var seeking by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(file) {
        val mp = MediaPlayer()
        mp.setOnPreparedListener {
            val real = it.duration.coerceAtLeast(0)
            duration = real
            if (real > 0 && (storedDurationMs == null || abs(real - storedDurationMs) > DURATION_TOLERANCE_MS)) {
                latestOnDuration(real.toLong())
            }
            player = it
        }
        mp.setOnCompletionListener {
            playing = false
            position = 0
        }
        mp.setOnErrorListener { _, _, _ ->
            latestOnError()
            true
        }
        runCatching {
            mp.setDataSource(file.absolutePath)
            mp.prepareAsync()
        }.onFailure { latestOnError() }
        onDispose {
            player = null
            playing = false
            mp.release()
        }
    }
    // Pause when the screen goes to the background.
    LifecycleResumeEffect(player) {
        onPauseOrDispose {
            player?.takeIf { it.isPlaying }?.pause()
            playing = false
        }
    }
    LaunchedEffect(playing, player) {
        while (playing) {
            position = player?.currentPosition ?: 0
            delay(100)
        }
    }

    val toggle = {
        player?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                playing = false
            } else {
                mp.start()
                playing = true
            }
        }
    }
    val playLabel = stringResource(if (playing) R.string.moment_pause else R.string.moment_play)

    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val compact = maxHeight < COMPACT_PLAYER_HEIGHT
        val discSize = if (compact) 64.dp else 120.dp
        val buttonSize = if (compact) 56.dp else 72.dp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                // Room for the glows: the scroll container clips at its bounds.
                .padding(vertical = if (compact) 20.dp else 36.dp)
        ) {
            Box(
                Modifier
                    .size(discSize)
                    .glow(colors.mint, radius = 36.dp, alpha = if (playing) 0.4f else 0.18f, offsetY = 0.dp)
                    .background(colors.mint.copy(alpha = 0.14f), CircleShape)
                    .border(1.5.dp, colors.mint.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Mic, contentDescription = null, tint = colors.mint, modifier = Modifier.size(discSize * 0.43f))
            }
            Spacer(Modifier.height(if (compact) Spacing.sm else Spacing.xl))
            Slider(
                value = seeking ?: if (duration > 0) (position / duration.toFloat()).coerceIn(0f, 1f) else 0f,
                onValueChange = { seeking = it },
                onValueChangeFinished = {
                    val target = ((seeking ?: 0f) * duration).toInt()
                    player?.seekTo(target)
                    position = target
                    seeking = null
                },
                enabled = player != null,
                colors = SliderDefaults.colors(
                    thumbColor = colors.mint,
                    activeTrackColor = colors.mint,
                    inactiveTrackColor = colors.surfaceHigh,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth()) {
                Text(formatClipDuration(position.toLong()), style = DeepTalkTheme.type.label, color = colors.textSecondary)
                Spacer(Modifier.weight(1f))
                Text(formatClipDuration(duration.toLong()), style = DeepTalkTheme.type.label, color = colors.textSecondary)
            }
            Spacer(Modifier.height(if (compact) Spacing.xs else Spacing.md))
            Box(
                Modifier
                    .size(buttonSize)
                    .glow(colors.brand, radius = 24.dp, alpha = 0.3f)
                    .background(colors.brandGradient, CircleShape)
                    .pressable(enabled = player != null, pressedScale = 0.9f, role = Role.Button, onClickLabel = playLabel, onClick = { toggle() })
                    .semantics { contentDescription = playLabel },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = colors.brandOn,
                    modifier = Modifier.size(buttonSize / 2)
                )
            }
        }
    }
}

/** Below this the voice player uses its compact sizes (landscape phones). */
private val COMPACT_PLAYER_HEIGHT = 340.dp

/** Stored vs real clip length difference (ms) that triggers a metadata fix. */
private const val DURATION_TOLERANCE_MS = 500
