package com.alantech.boardgame.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun LoadingComponent(
) {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center){
        Box(
            modifier = Modifier.fillMaxSize()
                .blur(20.dp)
        )
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ){
            LoadingSpinner(Modifier)
        }
    }
}

@Preview
@Composable
private fun LoadingPV() {
    LoadingComponent()
}
@Composable
fun LoadingSpinner(modifier: Modifier = Modifier) {
    val animation = rememberInfiniteTransition()
    val rotation = animation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000)
        ), label = "rotation"
    )
    Box(modifier = modifier.graphicsLayer { rotationZ = rotation.value }) {
        GradientCircle(color = Color(0xFF901AD5))
        GradientCircle(color = Color(0xFF5C1AD5), delay = 200)
        GradientCircle(color = Color(0xFF3D1AD5), delay = 400)
    }
}

@Composable
fun GradientCircle(
    modifier: Modifier = Modifier,
    color: Color,
    delay: Int = 0,
) {
    val animation = rememberInfiniteTransition()
    val rotation = animation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_000, easing = LinearEasing),
            initialStartOffset = StartOffset(delay)
        ), label = "gradientCircleRotation"
    )
    Box(
        modifier = modifier
            .graphicsLayer { rotationX = rotation.value; cameraDistance = 100000f }
            .fillMaxSize()
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f), color.copy(alpha = 0.3f)),
                        center = Offset.Zero,
                        radius = size.width,
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f), color.copy(alpha = 0.3f)),
                        center = Offset.Zero,
                        radius = size.width * 1.5f,
                    ),
                    style = Stroke(width = 1f)
                )
            }
    )
}