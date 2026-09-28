package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.boardgame.deepdeck.ui.theme.DeepTalkPalette
import com.boardgame.deepdeck.ui.theme.LocalReducedMotion
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val spin: Float,
    val isRect: Boolean,
)

/**
 * Canvas particle burst from the center (gold sparkles on the daily card,
 * confetti on win). Plays each time [trigger] changes to a non-null value.
 * Draws nothing under reduced motion.
 */
@Composable
fun ParticleBurst(
    trigger: Any?,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(DeepTalkPalette.Gold, Color(0xFFFFE3A3), DeepTalkPalette.Ember),
    particleCount: Int = 28,
    durationMs: Int = 1100,
    spread: Float = 1f,
    gravity: Float = 0.9f,
) {
    if (LocalReducedMotion.current) return
    val progress = remember { Animatable(1f) }
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    LaunchedEffect(trigger) {
        if (trigger == null) return@LaunchedEffect
        particles = List(particleCount) {
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = (0.35f + Random.nextFloat() * 0.65f) * spread,
                size = 3f + Random.nextFloat() * 5f,
                color = colors[it % colors.size],
                spin = Random.nextFloat() * 720f - 360f,
                isRect = Random.nextBoolean(),
            )
        }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
    }
    Canvas(modifier) {
        val t = progress.value
        if (t >= 1f || particles.isEmpty()) return@Canvas
        val center = Offset(size.width / 2f, size.height / 2f)
        val reach = size.minDimension * 0.75f
        val ease = 1f - (1f - t) * (1f - t)
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val dist = reach * p.speed * ease
            val pos = Offset(
                center.x + (cos(rad) * dist).toFloat(),
                center.y + (sin(rad) * dist).toFloat() + gravity * reach * 0.35f * t * t
            )
            val alpha = (1f - t).coerceIn(0f, 1f)
            val s = p.size * density * (1f - 0.4f * t)
            if (p.isRect) {
                rotate(p.spin * t, pos) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(pos.x - s / 2, pos.y - s / 4),
                        size = androidx.compose.ui.geometry.Size(s, s / 2)
                    )
                }
            } else {
                drawCircle(color = p.color.copy(alpha = alpha), radius = s / 2, center = pos)
            }
        }
    }
}
