package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.ui.theme.DeepTalkPalette
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion

// ---------------------------------------------------------------------------
// Level styling
// ---------------------------------------------------------------------------

val CardLevel.color: Color
    get() = when (this) {
        CardLevel.ICEBREAKER -> DeepTalkPalette.LevelIcebreaker
        CardLevel.DEEP -> DeepTalkPalette.LevelDeep
        CardLevel.INTIMATE -> DeepTalkPalette.LevelIntimate
        CardLevel.UNKNOWN -> DeepTalkPalette.Brand
    }

val CardLevel.icon: ImageVector
    get() = when (this) {
        CardLevel.ICEBREAKER -> Icons.Rounded.AcUnit
        CardLevel.DEEP -> Icons.Rounded.Waves
        CardLevel.INTIMATE -> Icons.Rounded.Favorite
        CardLevel.UNKNOWN -> Icons.Rounded.AutoAwesome
    }

@Composable
fun CardLevel.label(): String = when (this) {
    CardLevel.ICEBREAKER -> stringResource(R.string.level_icebreaker)
    CardLevel.DEEP -> stringResource(R.string.level_deep)
    CardLevel.INTIMATE -> stringResource(R.string.level_intimate)
    CardLevel.UNKNOWN -> stringResource(R.string.level_question)
}

// ---------------------------------------------------------------------------
// Flip
// ---------------------------------------------------------------------------

/**
 * 3D flip (rotationY 0→180°, 420ms FastOutSlowIn, cameraDistance 12·density,
 * face swapped at 90°). [front] shows while `flipped == false`.
 * Under reduced motion it crossfades instead.
 */
@Composable
fun FlipCard(
    flipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable BoxScope.() -> Unit,
    back: @Composable BoxScope.() -> Unit,
) {
    if (DeepTalkTheme.reducedMotion) {
        Crossfade(targetState = flipped, modifier = modifier, label = "flipCrossfade") { showBack ->
            Box(Modifier.fillMaxSize()) { if (showBack) back() else front() }
        }
        return
    }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(Motion.FLIP, easing = Motion.Flip),
        label = "flipRotation"
    )
    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = Motion.CARD_CAMERA_DISTANCE * density
        }
    ) {
        if (rotation <= 90f) {
            Box(Modifier.fillMaxSize()) { front() }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) { back() }
        }
    }
}

// ---------------------------------------------------------------------------
// Card faces
// ---------------------------------------------------------------------------

/**
 * Card face background: #2A1648 → #140A22 vertical + radial glow of the
 * level color at 18% from the top-left, hairline border.
 */
fun Modifier.cardFace(
    glowColor: Color,
    shape: Shape = DeepTalkShapes.xl,
): Modifier = this
    .clip(shape)
    .background(Brush.verticalGradient(listOf(DeepTalkPalette.CardFaceTop, DeepTalkPalette.CardFaceBottom)))
    .drawBehind {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glowColor.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = size.maxDimension * 0.85f
            ),
            radius = size.maxDimension * 0.85f,
            center = Offset(0f, 0f)
        )
    }
    .border(1.dp, Color.White.copy(alpha = 0.10f), shape)

/**
 * Question side of a card: level pill, Fraunces question, optional footer.
 * Reusable by gameplay (agent B) for the swipe stack.
 */
@Composable
fun QuestionCardFace(
    text: String,
    level: CardLevel,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = DeepTalkTheme.type.display,
    overline: String? = null,
    footer: (@Composable () -> Unit)? = null,
    shape: Shape = DeepTalkShapes.xl,
) {
    val colors = DeepTalkTheme.colors
    Column(
        modifier = modifier
            .cardFace(level.color, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        OverlinePill(text = overline ?: level.label(), color = level.color, icon = level.icon)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = text,
                style = textStyle,
                color = colors.textPrimary,
                overflow = TextOverflow.Ellipsis
            )
        }
        footer?.invoke()
    }
}

/**
 * Face-down side: brand gradient back with DeepTalk monogram glow.
 */
@Composable
fun CardBackFace(
    modifier: Modifier = Modifier,
    accent: Color = DeepTalkTheme.colors.brand,
    title: String? = null,
    subtitle: String? = null,
    shape: Shape = DeepTalkShapes.xl,
) {
    val colors = DeepTalkTheme.colors
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.9f), colors.brandStrong, Color(0xFF3A1470))
                )
            )
            .drawBehind {
                // Concentric rings pattern
                val c = Offset(size.width / 2, size.height / 2)
                for (i in 1..6) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.minDimension * 0.13f * i,
                        center = c,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.14f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            if (title != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = title,
                    style = DeepTalkTheme.type.displaySmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = subtitle.uppercase(),
                    style = DeepTalkTheme.type.overline,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
