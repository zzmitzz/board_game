package com.alantech.boardgame.features.ingame.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.ui.theme.LightSecondTextOBG

@Composable
fun EdgeToEdgeProgressBar(
    isEnable: Boolean = false,
    progress: Float, // Value between 0.0f and 1.0f
    modifier: Modifier = Modifier,
    ropeThickness: Dp = 4.dp,
    ropeColor: Color = LightSecondTextOBG, // Primary rope color
    trackColor: Color = Color(0xFF333333), // Inactive border outline color
    cardBackgroundColor: Color = Color.Transparent,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    // Smooth progress update animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ropeProgress"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .drawWithCache {
                val strokeWidthPx = ropeThickness.toPx()
                val radiusPx = cornerRadius.toPx()

                // Inset the path slightly so the stroke isn't clipped at the Canvas bounds
                val halfStroke = strokeWidthPx / 2f
                val cardBounds = androidx.compose.ui.geometry.Rect(
                    left = halfStroke,
                    top = halfStroke,
                    right = size.width - halfStroke,
                    bottom = size.height - halfStroke
                )

                // 1. Build the full rounded rectangle path
                val fullPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = cardBounds,
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                    )
                }

                // 2. Measure total perimeter length to extract the active progress segment
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(fullPath, false)
                val totalLength = pathMeasure.length

                val progressPath = Path()
                pathMeasure.getSegment(
                    startDistance = 0f,
                    stopDistance = totalLength * animatedProgress,
                    destination = progressPath,
                    startWithMoveTo = true
                )

                onDrawWithContent {
                    drawContent()
                    if(!isEnable){
                        return@onDrawWithContent
                    }
                    // Draw Card background interior
                    drawRoundRect(
                        color = cardBackgroundColor,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )

                    // Draw the background inactive border track
                    if (trackColor != Color.Transparent) {
                        drawPath(
                            path = fullPath,
                            color = trackColor,
                            style = Stroke(width = strokeWidthPx)
                        )
                    }

                    // Draw the active progress "rope" over the border
                    drawPath(
                        path = progressPath,
                        color = ropeColor,
                        style = Stroke(
                            width = strokeWidthPx,
                            cap = StrokeCap.Round // Creates rounded ends on the rope
                        )
                    )

                    // Render inner Card content on top
                }
            }
    ) {
        content()
    }
}