package com.boardgame.deepdeck.features.ingame.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.PlayerAvatar
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.model.GamePlayer
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion
import kotlin.math.hypot

/**
 * Pass-the-phone interstitial (plan §2.4): full-screen flood of the player's color as a
 * circular reveal from their avatar (450ms), "Pass to <name>", tap to reveal the card.
 * The base is opaque so the next card can never be peeked at. Crossfades under reduced motion.
 */
@Composable
fun PassThePhoneOverlay(
    player: GamePlayer,
    round: Int,
    totalRounds: Int,
    turnKey: Int,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val reduced = DeepTalkTheme.reducedMotion
    val haptics = LocalDeepTalkHaptics.current
    val reveal = remember(turnKey) { Animatable(if (reduced) 1f else 0f) }
    var origin by remember { mutableStateOf(Offset.Unspecified) }
    var rootOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(turnKey) {
        haptics.medium()
        if (!reduced) reveal.animateTo(1f, tween(Motion.PASS_REVEAL, easing = Motion.EmphasizedDecelerate))
    }

    val top = lerp(player.color, colors.bgBase, 0.35f)
    val bottom = lerp(player.color, colors.bgBase, 0.78f)
    val revealLabel = stringResource(R.string.pass_reveal_action, player.name)
    val doReveal = {
        haptics.confirm()
        onReveal()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { rootOffset = it.positionInRoot() }
            .drawBehind {
                drawRect(colors.bgBase)
                val center = if (origin.isSpecified) origin - rootOffset else Offset(size.width / 2, size.height * 0.36f)
                val maxRadius = listOf(
                    Offset.Zero, Offset(size.width, 0f), Offset(0f, size.height), Offset(size.width, size.height)
                ).maxOf { hypot(it.x - center.x, it.y - center.y) }
                val circle = Path().apply {
                    addOval(androidx.compose.ui.geometry.Rect(center, maxRadius * reveal.value))
                }
                clipPath(circle) {
                    drawRect(Brush.verticalGradient(listOf(top, bottom)))
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = revealLabel,
                onClick = doReveal
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .graphicsLayer { alpha = ((reveal.value - 0.35f) / 0.65f).coerceIn(0f, 1f) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlayerAvatar(
                player = player,
                size = 112.dp,
                ring = Color.White.copy(alpha = 0.85f),
                modifier = Modifier
                    .glow(player.color, radius = 40.dp, alpha = 0.45f, offsetY = 0.dp)
                    .onGloballyPositioned { coords ->
                        val b = coords.boundsInRoot()
                        origin = b.center
                    }
            )
            Spacer(Modifier.height(28.dp))
            Text(
                stringResource(R.string.pass_to).uppercase(),
                style = DeepTalkTheme.type.overline,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                player.name,
                style = DeepTalkTheme.type.hero,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.round_of, round, totalRounds),
                style = DeepTalkTheme.type.label,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
        val pulse = if (reduced) 1f else rememberInfiniteTransition(label = "passPulse").animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "passPulseAlpha"
        ).value
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .graphicsLayer { alpha = reveal.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.pass_tap_to_reveal),
                style = DeepTalkTheme.type.label,
                color = Color.White.copy(alpha = pulse),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            PrimaryButton(
                text = stringResource(R.string.pass_im_ready, player.name),
                onClick = doReveal,
                leadingIcon = Icons.Rounded.TouchApp,
                brush = Brush.linearGradient(listOf(Color.White, Color.White.copy(alpha = 0.88f))),
                contentColor = bottom,
                glowColor = player.color,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
