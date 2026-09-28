package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme

/**
 * App background: `bg.base` with slow-breathing aubergine/brand glows.
 * [accent] tints the top glow (e.g. a vibe or pack accent color).
 */
@Composable
fun GlowBackground(
    modifier: Modifier = Modifier,
    accent: Color = DeepTalkTheme.colors.brand,
    secondaryAccent: Color = DeepTalkTheme.colors.brandStrong,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val colors = DeepTalkTheme.colors
    val reduced = DeepTalkTheme.reducedMotion
    val transition = rememberInfiniteTransition(label = "glowBg")
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (reduced) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "breathe"
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase)
            .drawBehind {
                val w = size.width
                val r1 = w * (0.95f + 0.08f * breathe)
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(accent.copy(alpha = 0.22f + 0.05f * breathe), Color.Transparent),
                        center = Offset(w * 0.12f, -w * 0.08f),
                        radius = r1
                    ),
                    radius = r1,
                    center = Offset(w * 0.12f, -w * 0.08f)
                )
                val r2 = w * (0.8f + 0.06f * (1f - breathe))
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(secondaryAccent.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(w * 1.05f, w * 0.35f),
                        radius = r2
                    ),
                    radius = r2,
                    center = Offset(w * 1.05f, w * 0.35f)
                )
            },
        content = content
    )
}

/**
 * Glass surface: `surface` @ 72% + 1dp hairline outline, optional tint wash.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = DeepTalkShapes.lg,
    tint: Color? = null,
    borderColor: Color = DeepTalkTheme.colors.outline,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.glass, shape)
            .then(
                if (tint != null) Modifier.background(
                    Brush.linearGradient(listOf(tint.copy(alpha = 0.16f), Color.Transparent)),
                    shape
                ) else Modifier
            )
            .border(borderWidth, borderColor, shape),
        content = content
    )
}
