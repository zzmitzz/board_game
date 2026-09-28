package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SentimentDissatisfied
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing

/** Inline error with a retry action. Never leave the user on a silent empty screen. */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_title),
    message: String = stringResource(R.string.error_message),
    icon: ImageVector = Icons.Rounded.CloudOff,
    onRetry: (() -> Unit)? = null,
) {
    StateMessage(modifier, icon, DeepTalkTheme.colors.rose, title, message) {
        onRetry?.let {
            PrimaryButton(
                text = stringResource(R.string.retry),
                onClick = it,
                leadingIcon = Icons.Rounded.Refresh,
                height = 48.dp
            )
        }
    }
}

/** Neutral empty state. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.SentimentDissatisfied,
    action: (@Composable () -> Unit)? = null,
) {
    StateMessage(modifier, icon, DeepTalkTheme.colors.brand, title, message) { action?.invoke() }
}

@Composable
private fun StateMessage(
    modifier: Modifier,
    icon: ImageVector,
    accent: Color,
    title: String,
    message: String,
    action: @Composable () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxl, vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .glow(accent, radius = 28.dp, alpha = 0.3f, offsetY = 0.dp)
                .background(accent.copy(alpha = 0.14f), CircleShape)
                .border(1.dp, accent.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(Spacing.xxs))
        Text(title, style = DeepTalkTheme.type.title, color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(message, style = DeepTalkTheme.type.body, color = colors.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Spacing.xs))
        action()
    }
}

/** Rounded skeleton block with shimmer. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = DeepTalkShapes.md,
) {
    Box(
        modifier = modifier
            .background(DeepTalkTheme.colors.surface, shape)
            .shimmer()
    )
}

/** Skeleton text line. */
@Composable
fun SkeletonLine(width: Dp, modifier: Modifier = Modifier, height: Dp = 12.dp) {
    SkeletonBox(modifier.width(width).height(height), DeepTalkShapes.pill)
}

/**
 * Non-blocking offline banner (replaces the old full-screen dialog).
 * Slides in under the status bar; optional action (e.g. open Library).
 */
@Composable
fun OfflineBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = DeepTalkTheme.colors
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
            shape = DeepTalkShapes.pill,
            tint = colors.rose,
            borderColor = colors.rose.copy(alpha = 0.35f)
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.WifiOff, contentDescription = null, tint = colors.rose, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.offline_banner),
                    style = DeepTalkTheme.type.label,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )
                if (actionLabel != null && onAction != null) {
                    Text(
                        text = actionLabel,
                        style = DeepTalkTheme.type.label,
                        color = colors.brand,
                        modifier = Modifier
                            .pressable(onClick = onAction)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
