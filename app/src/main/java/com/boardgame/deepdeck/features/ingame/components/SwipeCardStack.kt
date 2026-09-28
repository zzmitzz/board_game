package com.boardgame.deepdeck.features.ingame.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.cardFace
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

enum class SwipeDirection { Left, Right }

/**
 * Gesture + animation state of the top card for one turn (re-created per `turnKey`).
 * Buttons use [fling] so they animate exactly like a swipe (accessible alternative).
 */
@Stable
class SwipeCardState internal constructor(private val reducedMotion: Boolean) {
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)
    /** 0 → 1 "next card" entrance (behind-stack 0.94 → 1, 12dp → 0). */
    val entry = Animatable(0f)
    var widthPx by mutableFloatStateOf(1f)
    var heightPx by mutableFloatStateOf(1f)
    var committed by mutableStateOf(false)
        private set

    /** Signed progress toward a commit: −1 (forfeit) … +1 (complete). */
    val dragProgress: Float
        get() = (offsetX.value / (widthPx * Motion.SWIPE_COMMIT_FRACTION)).coerceIn(-1f, 1f)

    /** Animates the card off-screen; returns false if this turn was already committed. */
    suspend fun fling(direction: SwipeDirection): Boolean {
        if (committed) return false
        committed = true
        if (reducedMotion) return true
        val target = (if (direction == SwipeDirection.Right) 1f else -1f) * widthPx * 1.6f
        offsetX.animateTo(target, tween(durationMillis = 240, easing = Motion.EmphasizedAccelerate))
        return true
    }

    suspend fun springBack() {
        kotlinx.coroutines.coroutineScope {
            launch { offsetX.animateTo(0f, Motion.swipeBackSpring()) }
            launch { offsetY.animateTo(0f, Motion.swipeBackSpring()) }
        }
    }
}

@Composable
fun rememberSwipeCardState(turnKey: Int): SwipeCardState {
    val reduced = DeepTalkTheme.reducedMotion
    return remember(turnKey, reduced) { SwipeCardState(reduced) }
}

/**
 * Tinder-style stack (plan §2.4): the top card follows the finger with
 * rotation = offsetX / width · 14°, commits past 35% width or 1200dp/s, otherwise springs back
 * (dampingRatio 0.55). Right = complete (mint ✓), left = forfeit (rose 💣), up / tap = flip.
 * Long-press opens the card menu. Under reduced motion dragging is disabled (buttons only)
 * and cards fade in instead of flying.
 */
@Composable
fun SwipeCardStack(
    state: SwipeCardState,
    turnKey: Int,
    enabled: Boolean,
    canFlip: Boolean,
    onFlip: () -> Unit,
    onLongPress: () -> Unit,
    onCommit: (SwipeDirection) -> Unit,
    modifier: Modifier = Modifier,
    behindCount: Int = 2,
    content: @Composable BoxScope.() -> Unit,
) {
    val reduced = DeepTalkTheme.reducedMotion
    val density = LocalDensity.current
    val haptics = LocalDeepTalkHaptics.current
    val scope = rememberCoroutineScope()
    val commitVelocity = with(density) { Motion.SWIPE_COMMIT_VELOCITY_DP.dp.toPx() }
    val stackOffset = with(density) { 12.dp.toPx() }
    val currentOnCommit by rememberUpdatedState(onCommit)
    val currentOnFlip by rememberUpdatedState(onFlip)
    val currentCanFlip by rememberUpdatedState(canFlip)

    LaunchedEffect(state) {
        if (reduced) state.entry.animateTo(1f, tween(Motion.SCREEN)) else state.entry.animateTo(1f, Motion.nextCardSpring())
    }

    val completeLabel = stringResource(R.string.complete_challenge)
    val forfeitLabel = stringResource(R.string.forfeit_and_penalty)
    val flipLabel = stringResource(R.string.flip_card)

    Box(modifier) {
        // Behind stack: blank card faces, 0.94 / 0.88 scale, 12dp / 24dp down.
        for (i in behindCount downTo 1) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val s = 1f - 0.06f * i
                        scaleX = s
                        scaleY = s
                        translationY = stackOffset * i * 1.6f
                        alpha = 1f - 0.28f * i
                    }
                    .cardFace(DeepTalkTheme.colors.brand)
            )
        }

        // Top card
        Box(
            Modifier
                .fillMaxSize()
                .onSizeChanged {
                    state.widthPx = it.width.toFloat().coerceAtLeast(1f)
                    state.heightPx = it.height.toFloat().coerceAtLeast(1f)
                }
                .semantics {
                    customActions = buildList {
                        add(CustomAccessibilityAction(completeLabel) { currentOnCommit(SwipeDirection.Right); true })
                        add(CustomAccessibilityAction(forfeitLabel) { currentOnCommit(SwipeDirection.Left); true })
                        if (currentCanFlip) add(CustomAccessibilityAction(flipLabel) { currentOnFlip(); true })
                    }
                }
                .pointerInput(turnKey, enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onTap = { if (currentCanFlip) currentOnFlip() },
                        onLongPress = {
                            haptics.medium()
                            onLongPress()
                        }
                    )
                }
                .pointerInput(turnKey, enabled, reduced) {
                    if (!enabled || reduced) return@pointerInput
                    val tracker = VelocityTracker()
                    var armed = false
                    detectDragGestures(
                        onDragStart = {
                            tracker.resetTracking()
                            armed = false
                        },
                        onDragCancel = { scope.launch { state.springBack() } },
                        onDragEnd = {
                            val velocity = tracker.calculateVelocity()
                            val x = state.offsetX.value
                            val y = state.offsetY.value
                            val horizontal = abs(x) >= abs(y)
                            val pastDistance = abs(x) > state.widthPx * Motion.SWIPE_COMMIT_FRACTION
                            val fastFling = abs(velocity.x) > commitVelocity &&
                                (x == 0f || sign(velocity.x) == sign(x))
                            val flipUp = !horizontal && currentCanFlip &&
                                (-y > state.heightPx * 0.18f || velocity.y < -commitVelocity)
                            when {
                                horizontal && (pastDistance || fastFling) -> {
                                    val dir = if ((if (x != 0f) x else velocity.x) > 0f) SwipeDirection.Right else SwipeDirection.Left
                                    scope.launch {
                                        if (state.fling(dir)) currentOnCommit(dir)
                                    }
                                }
                                flipUp -> {
                                    currentOnFlip()
                                    scope.launch { state.springBack() }
                                }
                                else -> scope.launch { state.springBack() }
                            }
                        },
                        onDrag = { change, drag ->
                            change.consume()
                            tracker.addPosition(change.uptimeMillis, change.position)
                            scope.launch {
                                state.offsetX.snapTo(state.offsetX.value + drag.x)
                                state.offsetY.snapTo(state.offsetY.value + drag.y)
                            }
                            val past = abs(state.offsetX.value) > state.widthPx * Motion.SWIPE_COMMIT_FRACTION
                            if (past && !armed) haptics.press()
                            armed = past
                        }
                    )
                }
                .graphicsLayer {
                    val e = state.entry.value
                    if (reduced) {
                        alpha = e
                    } else {
                        translationX = state.offsetX.value
                        translationY = state.offsetY.value * 0.35f + (1f - e) * stackOffset
                        rotationZ = state.offsetX.value / state.widthPx * Motion.SWIPE_MAX_ROTATION_DEG
                        val s = 0.94f + 0.06f * e
                        scaleX = s
                        scaleY = s
                    }
                }
        ) {
            content()
            SwipeOverlay(progress = state.dragProgress)
        }
    }
}

/** Mint ✓ (complete) / rose 💣 (forfeit) wash that grows with the drag. */
@Composable
private fun BoxScope.SwipeOverlay(progress: Float) {
    if (progress == 0f) return
    val colors = DeepTalkTheme.colors
    val complete = progress > 0f
    val amount = abs(progress)
    val tint = if (complete) colors.mint else colors.rose
    Box(
        Modifier
            .matchParentSize()
            .clip(DeepTalkShapes.xl)
            .background(tint.copy(alpha = 0.28f * amount))
    )
    Column(
        Modifier
            .align(if (complete) Alignment.TopStart else Alignment.TopEnd)
            .padding(24.dp)
            .graphicsLayer {
                alpha = amount
                val s = 0.7f + 0.3f * amount
                scaleX = s
                scaleY = s
                rotationZ = if (complete) -12f else 12f
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(tint),
            contentAlignment = Alignment.Center
        ) {
            if (complete) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.brandOn, modifier = Modifier.size(38.dp))
            } else {
                Image(
                    painter = painterResource(R.drawable.bomb_24px),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colors.brandOn),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Text(
            text = stringResource(if (complete) R.string.swipe_done else R.string.swipe_forfeit).uppercase(),
            style = DeepTalkTheme.type.overline,
            color = tint,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/**
 * Speed-mode timer drawn as a "rope" just outside the card (apply to a container padded
 * ~8dp around the stack). Colour runs mint → gold → rose as time runs out.
 */
fun Modifier.timerRing(enabled: Boolean, progress: Float, trackColor: Color, ropeColor: Color): Modifier =
    if (!enabled) this else this.drawWithCache {
        val stroke = 4.dp.toPx()
        val radius = 40.dp.toPx()
        val half = stroke / 2f
        val full = Path().apply {
            addRoundRect(RoundRect(Rect(half, half, size.width - half, size.height - half), CornerRadius(radius, radius)))
        }
        val measure = PathMeasure().apply { setPath(full, false) }
        val segment = Path()
        measure.getSegment(0f, measure.length * progress.coerceIn(0f, 1f), segment, true)
        onDrawWithContent {
            drawPath(full, trackColor, style = Stroke(stroke))
            drawPath(segment, ropeColor, style = Stroke(stroke, cap = StrokeCap.Round))
            drawContent()
        }
    }
