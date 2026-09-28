package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme

/**
 * Primary CTA: brand gradient pill (56dp) with a brand glow.
 * Pass [brush] to override (e.g. heat or gold gradient).
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    brush: Brush? = null,
    contentColor: Color = DeepTalkTheme.colors.brandOn,
    glowColor: Color = DeepTalkTheme.colors.brand,
    height: Dp = 56.dp,
) {
    val colors = DeepTalkTheme.colors
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .height(height)
            // Keep the modifier chain stable when `enabled` toggles (swapping nodes left the
            // gradient undrawn after a disabled → enabled change).
            .glow(glowColor, radius = 24.dp, alpha = if (enabled) 0.3f else 0f)
            // Shape-aware background instead of clip()+background(): with a clip layer the
            // gradient was not redrawn after `enabled` flipped false → true.
            .background(brush ?: colors.brandGradient, DeepTalkShapes.pill)
            .alpha(if (enabled) 1f else 0.45f)
            .pressable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = loading, label = "primaryLoading") { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    leadingIcon?.let {
                        Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = DeepTalkTheme.type.title,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    trailingIcon?.let {
                        Spacer(Modifier.width(8.dp))
                        Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

/** Secondary action: glass pill with hairline border. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    contentColor: Color = DeepTalkTheme.colors.textPrimary,
    height: Dp = 48.dp,
    enabled: Boolean = true,
) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier = modifier
            .height(height)
            .clip(DeepTalkShapes.pill)
            .background(colors.glass)
            .border(1.dp, colors.outline, DeepTalkShapes.pill)
            .alpha(if (enabled) 1f else 0.45f)
            .pressable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        leadingIcon?.let {
            Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = DeepTalkTheme.type.label, color = contentColor, maxLines = 1)
    }
}

/** Round glass icon button (back, search, close...). */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = DeepTalkTheme.colors.textPrimary,
    active: Boolean = false,
) {
    val colors = DeepTalkTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (active) colors.brand.copy(alpha = 0.22f) else colors.glass)
            .border(1.dp, if (active) colors.brand.copy(alpha = 0.5f) else colors.outline, CircleShape)
            .pressable(pressedScale = 0.9f, onClick = onClick, onClickLabel = contentDescription),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.48f))
    }
}
