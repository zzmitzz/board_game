package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.ui.theme.LocalReducedMotion
import com.boardgame.deepdeck.ui.theme.Motion
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Press
// ---------------------------------------------------------------------------

/** Scales to [pressedScale] while [interactionSource] is pressed (spring 0.6 / 800). */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = Motion.PRESS_SCALE,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.pressSpring(),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Clickable + press-scale + light haptic. The standard way to make any tile tappable.
 * [indication] defaults to none (the scale is the feedback).
 */
fun Modifier.pressable(
    enabled: Boolean = true,
    haptic: Boolean = true,
    pressedScale: Float = Motion.PRESS_SCALE,
    role: Role? = Role.Button,
    indication: Indication? = null,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val haptics = LocalDeepTalkHaptics.current
    this
        .pressScale(source, pressedScale)
        .clickable(
            interactionSource = source,
            indication = indication,
            enabled = enabled,
            role = role,
            onClickLabel = onClickLabel,
        ) {
            if (haptic) haptics.press()
            onClick()
        }
}

// ---------------------------------------------------------------------------
// Staggered list entrance: 40ms/item, translateY 16dp -> 0 + fade, 280ms, max 8
// ---------------------------------------------------------------------------

/** Remembers which entrance keys already played so lazy items don't re-animate on scroll. */
class EntranceTracker internal constructor() {
    internal val played = mutableSetOf<Any>()
}

@Composable
fun rememberEntranceTracker(): EntranceTracker = remember { EntranceTracker() }

fun Modifier.staggeredEntrance(
    index: Int,
    tracker: EntranceTracker? = null,
    key: Any = index,
): Modifier = composed {
    val reduced = LocalReducedMotion.current
    if (reduced || index >= Motion.STAGGER_MAX_ITEMS || tracker?.played?.contains(key) == true) {
        return@composed Modifier
    }
    val progress = remember { Animatable(0f) }
    val offsetPx = with(LocalDensity.current) { Motion.EntranceOffset.toPx() }
    LaunchedEffect(Unit) {
        delay(index * Motion.STAGGER_STEP)
        progress.animateTo(1f, tween(Motion.LIST_ENTRANCE, easing = Motion.EmphasizedDecelerate))
        tracker?.played?.add(key)
    }
    Modifier.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}

// ---------------------------------------------------------------------------
// Glow (elevation = glow, not shadow)
// ---------------------------------------------------------------------------

/**
 * Soft colored glow drawn behind the element (radius 24–40dp, 20–30% alpha).
 * Implemented with an elliptical radial gradient so it works on every API level.
 */
fun Modifier.glow(
    color: Color,
    radius: Dp = 32.dp,
    alpha: Float = 0.26f,
    offsetY: Dp = 8.dp,
): Modifier = drawBehind {
    val r = radius.toPx()
    val w = size.width + r * 2
    val h = size.height + r * 2
    val center = Offset(size.width / 2f, size.height / 2f + offsetY.toPx())
    val maxDim = maxOf(w, h)
    scale(scaleX = w / maxDim, scaleY = h / maxDim, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                0f to color.copy(alpha = alpha),
                0.55f to color.copy(alpha = alpha * 0.55f),
                1f to Color.Transparent,
                center = center,
                radius = maxDim / 2f,
            ),
            radius = maxDim / 2f,
            center = center,
        )
    }
}

// ---------------------------------------------------------------------------
// Shimmer / sheen
// ---------------------------------------------------------------------------

/** Continuous skeleton shimmer (loading placeholders). */
fun Modifier.shimmer(
    highlight: Color = Color.White.copy(alpha = 0.08f),
    periodMs: Int = Motion.SKELETON_PERIOD,
): Modifier = composed {
    if (LocalReducedMotion.current) return@composed Modifier
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing)),
        label = "shimmerProgress"
    )
    Modifier.drawWithContent {
        drawContent()
        val x = size.width * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, highlight, Color.Transparent),
                start = Offset(x - size.width * 0.5f, 0f),
                end = Offset(x + size.width * 0.5f, size.height),
            )
        )
    }
}

/** Diagonal light sweep every [periodMs] (Tonight's Card face-down state). */
fun Modifier.sheenSweep(
    periodMs: Int = Motion.SHIMMER_PERIOD,
    sweepMs: Int = 1100,
    highlight: Color = Color.White.copy(alpha = 0.18f),
): Modifier = composed {
    if (LocalReducedMotion.current) return@composed Modifier
    val transition = rememberInfiniteTransition(label = "sheen")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = periodMs
                0f at 0
                0f at (periodMs - sweepMs) using LinearEasing
                1f at periodMs
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenProgress"
    )
    Modifier.drawWithContent {
        drawContent()
        if (progress <= 0f || progress >= 1f) return@drawWithContent
        val band = size.width * 0.45f
        val x = -band + (size.width + band * 2) * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, highlight, Color.Transparent),
                start = Offset(x - band, 0f),
                end = Offset(x, size.height),
            )
        )
    }
}
