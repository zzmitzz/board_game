package com.boardgame.deepdeck.features.ingame.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.features.ingame.InGameUiEffect
import com.boardgame.deepdeck.features.ingame.InGameUiState
import com.boardgame.deepdeck.features.ingame.InGameVM
import com.boardgame.deepdeck.features.ingame.components.PassThePhoneOverlay
import com.boardgame.deepdeck.features.ingame.components.SwipeCardStack
import com.boardgame.deepdeck.features.ingame.components.SwipeDirection
import com.boardgame.deepdeck.features.ingame.components.rememberSwipeCardState
import com.boardgame.deepdeck.features.ingame.components.timerRing
import com.boardgame.deepdeck.features.ingame.model.GameConfig
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.ErrorState
import com.boardgame.deepdeck.ui.components.FlipCard
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.OverlinePill
import com.boardgame.deepdeck.ui.components.PlayerAvatar
import com.boardgame.deepdeck.ui.components.QuestionCardFace
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.ToggleRow
import com.boardgame.deepdeck.ui.components.color
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.shimmer
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.shareImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Stateful Play screen. [onFinished] → End ("Wrapped"); [onQuit] → confirmed exit, nothing saved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreenStateful(
    vm: InGameVM,
    onFinished: () -> Unit,
    onQuit: () -> Unit,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val timeLeft by vm.timeLeft.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    val context = LocalContext.current
    val haptics = LocalDeepTalkHaptics.current
    val scope = rememberCoroutineScope()

    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showPauseSheet by rememberSaveable { mutableStateOf(false) }
    var showCardMenu by rememberSaveable { mutableStateOf(false) }
    var showHint by rememberSaveable { mutableStateOf(false) }
    var roundBanner by remember { mutableIntStateOf(0) }

    val playing = uiState as? InGameUiState.Playing
    val turnKey = playing?.turnKey ?: 0
    val swipeState = rememberSwipeCardState(turnKey)
    var flipped by remember(turnKey) { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.ensureStarted() }
    LifecycleStartEffect(vm) {
        vm.setForeground(true)
        onStopOrDispose { vm.setForeground(false) }
    }
    // Pause the timer while any overlay is open.
    LaunchedEffect(showExitDialog, showPauseSheet, showCardMenu, showHint) {
        vm.setPaused(showExitDialog || showPauseSheet || showCardMenu || showHint)
    }
    LaunchedEffect(uiState) {
        if (uiState is InGameUiState.Finished) onFinished()
    }

    val commit: (SwipeDirection, Int) -> Unit = { direction, key ->
        if (direction == SwipeDirection.Right) {
            haptics.confirm()
            vm.complete(key)
        } else {
            haptics.doubleTick()
            vm.forfeit(key)
        }
    }
    val flingThenCommit: (SwipeDirection) -> Unit = { direction ->
        val key = turnKey
        scope.launch { if (swipeState.fling(direction)) commit(direction, key) }
    }

    // The effect collector outlives turns: always use the latest swipe state / turn key.
    val latestFling by rememberUpdatedState(flingThenCommit)
    val latestTurnKey by rememberUpdatedState(turnKey)

    LaunchedEffect(vm) {
        vm.uiEffect.collect { effect ->
            when (effect) {
                is InGameUiEffect.ShowMessage -> launch { snackbar.showSnackbar(context.getString(effect.message)) }
                is InGameUiEffect.TimeUp -> if (effect.turnKey == latestTurnKey) {
                    launch { snackbar.showSnackbar(context.getString(R.string.time_up)) }
                    latestFling(SwipeDirection.Left)
                }
                is InGameUiEffect.RoundStarted -> {
                    haptics.medium()
                    roundBanner = effect.round
                }
                is InGameUiEffect.DeckReshuffled ->
                    launch { snackbar.showSnackbar(context.getString(R.string.deck_reshuffled)) }
                is InGameUiEffect.GameFinished -> Unit // End screen celebrates (confetti + win haptic)
                is InGameUiEffect.Share -> context.shareImage(
                    effect.uri,
                    chooserTitle = context.getString(R.string.share),
                    fallbackText = effect.fallbackText
                )
            }
        }
    }

    BackHandler(enabled = uiState is InGameUiState.Playing || uiState is InGameUiState.Loading) {
        showExitDialog = true
    }

    GlowBackground(accent = playing?.player?.color?.let { lerp(it, DeepTalkTheme.colors.brand, 0.5f) } ?: DeepTalkTheme.colors.brand) {
        when (val state = uiState) {
            InGameUiState.Idle, InGameUiState.Loading -> LoadingDeck(onClose = { showExitDialog = true })
            is InGameUiState.Error -> Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.Center
            ) {
                ErrorState(
                    message = stringResource(state.message),
                    onRetry = if (state.message == R.string.game_load_error) vm::retry else null
                )
                Spacer(Modifier.height(Spacing.md))
                GhostButton(
                    text = stringResource(R.string.back),
                    onClick = onQuit,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            InGameUiState.Finished -> Box(Modifier.fillMaxSize())
            is InGameUiState.Playing -> {
                PlayingContent(
                    state = state,
                    timeLeft = timeLeft,
                    flipped = flipped,
                    swipeContent = {
                        SwipeCardStack(
                            state = swipeState,
                            turnKey = state.turnKey,
                            enabled = !state.awaitingReveal && !state.paused,
                            canFlip = state.card.backSide != null,
                            onFlip = { flipped = !flipped; haptics.press() },
                            onLongPress = { showCardMenu = true },
                            onCommit = { dir -> commit(dir, state.turnKey) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            GameCard(state = state, flipped = flipped, onHintClick = { showHint = true })
                        }
                    },
                    onPause = { showPauseSheet = true },
                    onToggleSave = vm::toggleSave,
                    onMenu = { showCardMenu = true },
                    onComplete = { flingThenCommit(SwipeDirection.Right) },
                    onForfeit = { flingThenCommit(SwipeDirection.Left) },
                    onSkip = {
                        haptics.press()
                        vm.skip(state.turnKey)
                    },
                )
                RoundBanner(round = roundBanner, total = state.totalRounds, onShown = { roundBanner = 0 })
                AnimatedVisibility(
                    visible = state.awaitingReveal,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PassThePhoneOverlay(
                        player = state.player,
                        round = state.round,
                        totalRounds = state.totalRounds,
                        turnKey = state.turnKey,
                        onReveal = vm::revealTurn
                    )
                }
            }
        }
    }

    // --- Overlays -------------------------------------------------------------

    if (showExitDialog) {
        ExitGameDialog(
            onConfirm = {
                showExitDialog = false
                vm.quit()
                onQuit()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    if (showPauseSheet && playing != null) {
        ModalBottomSheet(
            onDismissRequest = { showPauseSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DeepTalkTheme.colors.bgElevated,
        ) {
            PauseSheetContent(
                state = playing,
                onResume = { showPauseSheet = false },
                onSoundChange = { vm.setSound(it) },
                onHapticsChange = { vm.setHaptics(it) },
                onQuit = {
                    showPauseSheet = false
                    showExitDialog = true
                }
            )
        }
    }

    if (showCardMenu && playing != null) {
        ModalBottomSheet(
            onDismissRequest = { showCardMenu = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DeepTalkTheme.colors.bgElevated,
        ) {
            CardMenuContent(
                state = playing,
                onToggleSave = {
                    vm.toggleSave()
                    showCardMenu = false
                },
                onShare = {
                    vm.shareCurrentCard()
                    showCardMenu = false
                },
                onHint = if (playing.card.hint.isNotBlank()) ({
                    showCardMenu = false
                    showHint = true
                }) else null,
                onReport = if (!playing.isCustomPack) ({
                    vm.report()
                    showCardMenu = false
                }) else null,
            )
        }
    }

    if (showHint && playing != null) {
        AlertDialog(
            onDismissRequest = { showHint = false },
            containerColor = DeepTalkTheme.colors.surfaceHigh,
            icon = { Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = DeepTalkTheme.colors.gold) },
            title = { Text(stringResource(R.string.card_hint), style = DeepTalkTheme.type.headline) },
            text = { Text(playing.card.hint, style = DeepTalkTheme.type.body, color = DeepTalkTheme.colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { showHint = false }) {
                    Text(stringResource(R.string.got_it), color = DeepTalkTheme.colors.brand)
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Playing layout
// ---------------------------------------------------------------------------

@Composable
private fun PlayingContent(
    state: InGameUiState.Playing,
    timeLeft: Float,
    flipped: Boolean,
    swipeContent: @Composable () -> Unit,
    onPause: () -> Unit,
    onToggleSave: () -> Unit,
    onMenu: () -> Unit,
    onComplete: () -> Unit,
    onForfeit: () -> Unit,
    onSkip: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar: pause · round progress · save · menu
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(Icons.Rounded.Pause, stringResource(R.string.pause_game), onPause, size = 40.dp)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    state.packTitle,
                    style = DeepTalkTheme.type.label,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                RoundProgress(round = state.round, total = state.totalRounds)
            }
            GlassIconButton(
                icon = if (state.isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(if (state.isSaved) R.string.unsave_card else R.string.save_card_action),
                onClick = onToggleSave,
                size = 40.dp,
                tint = if (state.isSaved) colors.rose else colors.textPrimary,
                active = state.isSaved
            )
            Spacer(Modifier.width(8.dp))
            GlassIconButton(Icons.Rounded.MoreHoriz, stringResource(R.string.card_options), onMenu, size = 40.dp)
        }

        // Whose turn
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(state.player, size = 44.dp, ring = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.player_turn_format, state.player.name),
                    style = DeepTalkTheme.type.displaySmall,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (state.speedMode) {
                    Text(
                        stringResource(R.string.seconds_left, timeLeft.toInt().coerceAtLeast(0)),
                        style = DeepTalkTheme.type.label,
                        color = timerColor(timeLeft)
                    )
                }
            }
            UpNext(state)
        }

        // Card stack (+ speed-mode timer ring)
        val ringProgress = timeLeft / GameConfig.TURN_SECONDS
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter - 8.dp, vertical = Spacing.xs)
                .timerRing(
                    enabled = state.speedMode && !state.awaitingReveal,
                    progress = ringProgress,
                    trackColor = colors.outline,
                    ropeColor = timerColor(timeLeft)
                )
                .padding(8.dp)
                .padding(bottom = 30.dp)
        ) {
            swipeContent()
        }

        state.penaltyText?.let { penalty ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter)
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OverlinePill(stringResource(R.string.penalty_format, penalty), colors.rose)
            }
        }

        // Accessible button alternatives to swiping
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundAction(
                label = stringResource(R.string.forfeit_short),
                description = stringResource(R.string.forfeit_and_penalty),
                color = colors.rose,
                size = 68.dp,
                onClick = onForfeit,
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.bomb_24px),
                    contentDescription = null,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colors.rose),
                    modifier = Modifier.size(28.dp)
                )
            }
            RoundAction(
                label = stringResource(R.string.skip_free),
                description = stringResource(if (state.canSkip) R.string.skip_card_desc else R.string.skip_used),
                color = colors.textSecondary,
                size = 52.dp,
                enabled = state.canSkip,
                onClick = onSkip,
            ) {
                Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = colors.textSecondary)
            }
            RoundAction(
                label = stringResource(R.string.complete_short),
                description = stringResource(R.string.complete_challenge),
                color = colors.mint,
                size = 68.dp,
                onClick = onComplete,
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.mint, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun GameCard(state: InGameUiState.Playing, flipped: Boolean, onHintClick: () -> Unit) {
    val colors = DeepTalkTheme.colors
    val level = state.level
    val footer: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (state.card.backSide != null) {
                Icon(Icons.Rounded.Sync, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(if (flipped) R.string.tap_to_flip_back else R.string.tap_to_flip),
                    style = DeepTalkTheme.type.label,
                    color = colors.textMuted
                )
            } else {
                Text(
                    stringResource(R.string.swipe_hint),
                    style = DeepTalkTheme.type.label,
                    color = colors.textMuted
                )
            }
            Spacer(Modifier.weight(1f))
            if (state.card.hint.isNotBlank()) {
                Row(
                    Modifier
                        .clip(DeepTalkShapes.pill)
                        .background(colors.gold.copy(alpha = 0.14f))
                        .pressable(onClick = onHintClick, onClickLabel = stringResource(R.string.card_hint))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = colors.gold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.card_hint), style = DeepTalkTheme.type.label, color = colors.gold)
                }
            }
        }
    }
    FlipCard(
        flipped = flipped,
        modifier = Modifier
            .fillMaxSize()
            .glow(level.color, radius = 32.dp, alpha = 0.22f),
        front = {
            QuestionCardFace(
                text = state.card.description,
                level = level,
                modifier = Modifier.fillMaxSize(),
                textStyle = cardTextStyle(state.card.description),
                footer = footer
            )
        },
        back = {
            QuestionCardFace(
                text = state.card.backSide.orEmpty(),
                level = level,
                modifier = Modifier.fillMaxSize(),
                textStyle = cardTextStyle(state.card.backSide.orEmpty()),
                overline = stringResource(R.string.card_flip_side),
                footer = footer
            )
        }
    )
}

/** Long questions step down from the 30sp display size so they never clip. */
@Composable
private fun cardTextStyle(text: String) = when {
    text.length > 180 -> DeepTalkTheme.type.displaySmall.copy(fontSize = DeepTalkTheme.type.displaySmall.fontSize * 0.9f)
    text.length > 110 -> DeepTalkTheme.type.displaySmall
    else -> DeepTalkTheme.type.display
}

@Composable
private fun timerColor(timeLeft: Float): Color {
    val colors = DeepTalkTheme.colors
    val t = (timeLeft / GameConfig.TURN_SECONDS).coerceIn(0f, 1f)
    return when {
        t > 0.5f -> lerp(colors.gold, colors.mint, (t - 0.5f) * 2f)
        else -> lerp(colors.rose, colors.gold, t * 2f)
    }
}

@Composable
private fun RoundProgress(round: Int, total: Int) {
    val colors = DeepTalkTheme.colors
    val description = stringResource(R.string.round_of, round, total)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics { contentDescription = description }
    ) {
        Text(
            description.uppercase(),
            style = DeepTalkTheme.type.overline,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            val segments = total.coerceAtMost(20)
            repeat(segments) { i ->
                Box(
                    Modifier
                        .width(if (segments > 12) 6.dp else 10.dp)
                        .height(4.dp)
                        .clip(DeepTalkShapes.pill)
                        .background(if (i < round) colors.brand else colors.surfaceHigh)
                )
            }
        }
    }
}

@Composable
private fun UpNext(state: InGameUiState.Playing) {
    val upcoming = (1..minOf(3, state.players.size - 1)).map { state.players[(state.playerIndex + it) % state.players.size] }
    if (upcoming.isEmpty()) return
    val description = stringResource(R.string.up_next, upcoming.first().name)
    Box(Modifier.semantics { contentDescription = description }) {
        upcoming.reversed().forEachIndexed { i, player ->
            val index = upcoming.size - 1 - i
            PlayerAvatar(
                player,
                size = 26.dp,
                ring = DeepTalkTheme.colors.bgBase,
                modifier = Modifier.offset(x = (index * 16).dp)
            )
        }
        Spacer(Modifier.width((26 + (upcoming.size - 1) * 16).dp))
    }
}

@Composable
private fun RoundAction(
    label: String,
    description: String,
    color: Color,
    size: Dp,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(size)
                .then(if (enabled) Modifier.glow(color, radius = 20.dp, alpha = 0.22f, offsetY = 4.dp) else Modifier)
                .clip(CircleShape)
                .background(color.copy(alpha = if (enabled) 0.14f else 0.05f))
                .border(1.5.dp, color.copy(alpha = if (enabled) 0.6f else 0.2f), CircleShape)
                .pressable(enabled = enabled, pressedScale = 0.9f, role = Role.Button, onClickLabel = description, onClick = onClick)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) { icon() }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = DeepTalkTheme.type.label,
            color = if (enabled) color else DeepTalkTheme.colors.textMuted
        )
    }
}

@Composable
private fun RoundBanner(round: Int, total: Int, onShown: () -> Unit) {
    LaunchedEffect(round) {
        if (round > 0) {
            delay(1600)
            onShown()
        }
    }
    Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = round > 0,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            val colors = DeepTalkTheme.colors
            Text(
                text = stringResource(R.string.round_started, round, total),
                style = DeepTalkTheme.type.title,
                color = colors.brandOn,
                modifier = Modifier
                    .padding(top = 64.dp)
                    .glow(colors.brand, radius = 24.dp, alpha = 0.4f)
                    .clip(DeepTalkShapes.pill)
                    .background(colors.brandGradient)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun LoadingDeck(onClose: () -> Unit) {
    val colors = DeepTalkTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(Spacing.gutter)
    ) {
        GlassIconButton(Icons.Rounded.Close, stringResource(R.string.quit_game), onClose, size = 40.dp)
        Spacer(Modifier.height(Spacing.xl))
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ) {
            SkeletonBox(Modifier.fillMaxSize(), shape = DeepTalkShapes.xl)
        }
        Text(
            stringResource(R.string.shuffling_cards),
            style = DeepTalkTheme.type.title,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.xl)
                .shimmer()
        )
    }
}

@Composable
private fun ExitGameDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = DeepTalkTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceHigh,
        icon = { Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, tint = colors.rose) },
        title = { Text(stringResource(R.string.confirm_exit_title), style = DeepTalkTheme.type.headline, color = colors.textPrimary) },
        text = { Text(stringResource(R.string.quit_game_message), style = DeepTalkTheme.type.body, color = colors.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.quit_game), color = colors.rose) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.keep_playing), color = colors.brand) }
        }
    )
}

@Composable
private fun PauseSheetContent(
    state: InGameUiState.Playing,
    onResume: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onQuit: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Column(Modifier.padding(bottom = Spacing.xl)) {
        Text(
            stringResource(R.string.game_paused),
            style = DeepTalkTheme.type.headline,
            color = colors.textPrimary,
            modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm)
        )
        ToggleRow(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            title = stringResource(R.string.enable_sound),
            description = null,
            checked = state.soundOn,
            onCheckedChange = onSoundChange
        )
        ToggleRow(
            icon = Icons.Rounded.Vibration,
            title = stringResource(R.string.enable_haptics),
            description = null,
            checked = state.hapticsOn,
            onCheckedChange = onHapticsChange
        )
        HorizontalDivider(color = colors.outline, modifier = Modifier.padding(vertical = Spacing.xs))
        SheetAction(Icons.Rounded.PlayArrow, stringResource(R.string.resume_game), colors.brand, onResume)
        SheetAction(Icons.AutoMirrored.Rounded.ExitToApp, stringResource(R.string.quit_game), colors.rose, onQuit)
    }
}

@Composable
private fun CardMenuContent(
    state: InGameUiState.Playing,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onHint: (() -> Unit)?,
    onReport: (() -> Unit)?,
) {
    val colors = DeepTalkTheme.colors
    Column(Modifier.padding(bottom = Spacing.xl)) {
        Text(
            state.card.description,
            style = DeepTalkTheme.type.title,
            color = colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm)
        )
        SheetAction(
            if (state.isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            stringResource(if (state.isSaved) R.string.unsave_card else R.string.save_card_action),
            colors.rose,
            onToggleSave
        )
        SheetAction(Icons.Rounded.IosShare, stringResource(R.string.share_card_image), colors.brand, onShare)
        onHint?.let { SheetAction(Icons.Rounded.Lightbulb, stringResource(R.string.card_hint), colors.gold, it) }
        onReport?.let { SheetAction(Icons.Rounded.Flag, stringResource(R.string.report_card), colors.textSecondary, it) }
    }
}

@Composable
private fun SheetAction(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .pressable(pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = Spacing.gutter, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(16.dp))
        Text(label, style = DeepTalkTheme.type.title, color = DeepTalkTheme.colors.textPrimary)
    }
}
