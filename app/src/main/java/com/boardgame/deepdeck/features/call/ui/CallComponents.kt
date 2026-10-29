package com.boardgame.deepdeck.features.call.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Round call control. [off] = the feature is off (muted, camera off): drawn light so it stands out. */
@Composable
fun CallControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    off: Boolean = false,
    danger: Boolean = false,
    enabled: Boolean = true,
    badge: Int = 0,
    size: Dp = 56.dp,
) {
    val colors = DeepTalkTheme.colors
    val background = when {
        danger -> colors.rose
        off -> colors.textPrimary
        else -> Color.White.copy(alpha = 0.14f)
    }
    val tint = when {
        danger -> Color.White
        off -> colors.bgBase
        else -> colors.textPrimary
    }
    Box(modifier) {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(background.copy(alpha = if (enabled) background.alpha else background.alpha * 0.4f))
                .border(1.dp, if (danger || off) Color.Transparent else colors.outline, CircleShape)
                .pressable(enabled = enabled, pressedScale = 0.9f, onClickLabel = contentDescription, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = contentDescription, tint = tint.copy(alpha = if (enabled) 1f else 0.5f), modifier = Modifier.size(size * 0.45f))
        }
        if (badge > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(colors.rose, CircleShape)
                    .border(2.dp, colors.bgBase, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (badge > 9) "9+" else badge.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Small glass pill: the call timer, "Connecting…", the room code. [dot] = a live status light. */
@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier, dot: Color? = null) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier
            .clip(DeepTalkShapes.pill)
            .background(Color.Black.copy(alpha = 0.38f))
            .border(1.dp, colors.outline, DeepTalkShapes.pill)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot != null) {
            val pulse by rememberInfiniteTransition(label = "dot").animateFloat(
                initialValue = 1f,
                targetValue = if (DeepTalkTheme.reducedMotion) 1f else 0.4f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "dotAlpha"
            )
            Box(Modifier.size(8.dp).background(dot.copy(alpha = pulse), CircleShape))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = DeepTalkTheme.type.label, color = colors.textPrimary, maxLines = 1)
    }
}

/** A warning line under the top bar (reconnecting, network trouble). */
@Composable
fun CallBanner(text: String, icon: ImageVector, modifier: Modifier = Modifier, tint: Color = DeepTalkTheme.colors.gold) {
    Row(
        modifier
            .clip(DeepTalkShapes.md)
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, tint.copy(alpha = 0.35f), DeepTalkShapes.md)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = DeepTalkTheme.type.label, color = DeepTalkTheme.colors.textPrimary)
    }
}

/** Stands in for a video that is off: the person's initial on a soft gradient. */
@Composable
fun AvatarBackdrop(
    name: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = DeepTalkTheme.colors
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier.background(Brush.verticalGradient(listOf(colors.surfaceHigh, colors.bgBase))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(if (compact) 48.dp else 112.dp)
                    .background(Brush.linearGradient(listOf(colors.brand, colors.rose)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initial,
                    style = if (compact) DeepTalkTheme.type.headline else DeepTalkTheme.type.hero,
                    color = Color.White
                )
            }
            if (!compact) {
                Spacer(Modifier.height(Spacing.md))
                Text(name, style = DeepTalkTheme.type.headline, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = DeepTalkTheme.type.label, color = colors.textSecondary)
                }
            }
        }
    }
}

/** The room code, big enough to read aloud, with Copy and Share. */
@Composable
fun InviteCard(
    code: String,
    title: String,
    description: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    waiting: Boolean = false,
) {
    val colors = DeepTalkTheme.colors
    GlassSurface(modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (waiting) {
                val pulse by rememberInfiniteTransition(label = "waiting").animateFloat(
                    initialValue = 0.85f,
                    targetValue = if (DeepTalkTheme.reducedMotion) 0.85f else 1.1f,
                    animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
                    label = "pulse"
                )
                Box(
                    Modifier
                        .size(14.dp)
                        .scale(pulse)
                        .background(colors.mint, CircleShape)
                )
                Spacer(Modifier.height(Spacing.sm))
            }
            Text(title, style = DeepTalkTheme.type.headline, color = colors.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(description, style = DeepTalkTheme.type.body, color = colors.textSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Spacing.md))
            Text(
                code.chunked(3).joinToString(" "),
                style = DeepTalkTheme.type.hero.copy(letterSpacing = 6.sp),
                color = colors.textPrimary,
                modifier = Modifier.pressable(role = Role.Button, onClickLabel = stringResource(R.string.call_invite_copy), onClick = onCopy)
            )
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                GhostButton(text = stringResource(R.string.call_invite_copy), onClick = onCopy, leadingIcon = Icons.Rounded.ContentCopy)
                GhostButton(text = stringResource(R.string.call_invite_share), onClick = onShare, leadingIcon = Icons.Rounded.IosShare)
            }
        }
    }
}

/**
 * The self view: a small window the user can drag, which snaps to the nearest corner.
 * [topInset]/[bottomInset] keep it clear of the top bar and the controls.
 */
@Composable
fun DraggablePip(
    modifier: Modifier = Modifier,
    topInset: Dp = 72.dp,
    bottomInset: Dp = 120.dp,
    width: Dp = 104.dp,
    height: Dp = 156.dp,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(modifier.fillMaxSize().padding(horizontal = Spacing.md)) {
        val density = LocalDensity.current
        val w = with(density) { width.toPx() }
        val h = with(density) { height.toPx() }
        val minY = with(density) { topInset.toPx() }
        val maxX = (constraints.maxWidth - w).coerceAtLeast(0f)
        val maxY = (constraints.maxHeight - h - with(density) { bottomInset.toPx() }).coerceAtLeast(minY)
        // Starts bottom-right, above the controls.
        val offset = remember(maxX, maxY) { Animatable(Offset(maxX, maxY), Offset.VectorConverter) }

        Box(
            Modifier
                .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
                .size(width, height)
                .clip(DeepTalkShapes.md)
                .border(1.5.dp, Color.White.copy(alpha = 0.25f), DeepTalkShapes.md)
                .pointerInput(maxX, maxY) {
                    detectDragGestures(
                        onDragEnd = {
                            val target = Offset(
                                x = if (offset.value.x + w / 2 < maxX / 2 + w / 2) 0f else maxX,
                                y = if (offset.value.y + h / 2 < (minY + maxY) / 2 + h / 2) minY else maxY,
                            )
                            scope.launch {
                                offset.animateTo(target, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    ) { change, drag ->
                        change.consume()
                        scope.launch {
                            offset.snapTo(
                                Offset(
                                    (offset.value.x + drag.x).coerceIn(0f, maxX),
                                    (offset.value.y + drag.y).coerceIn(minY, maxY),
                                )
                            )
                        }
                    }
                }
        ) {
            content()
        }
    }
}
