package com.alantech.boardgame.features.gameend.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun Modifier.staggeredEntrance(
    index: Int,
    delayPerItemMillis: Long = 50L,
    initialOffsetY: Dp = 40.dp
): Modifier {
    // Track alpha and vertical translation offset
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(initialOffsetY.value) }

    LaunchedEffect(Unit) {
        // Calculate stagger delay based on the item index
        delay((index * delayPerItemMillis).milliseconds)

        // Animate alpha and offset simultaneously
        alpha.animateTo(1f, animationSpec = tween(durationMillis = 300))
        offsetY.animateTo(0f, animationSpec = tween(durationMillis = 300))
    }

    return this
        .graphicsLayer {
            this.alpha = alpha.value
            this.translationY = offsetY.value * density
        }
}