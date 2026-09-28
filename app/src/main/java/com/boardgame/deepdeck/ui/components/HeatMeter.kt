package com.boardgame.deepdeck.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion

/** Heat bands on the canonical 0–100 scale (§5.1.12). */
enum class HeatBand(@StringRes val label: Int, val range: IntRange) {
    MILD(R.string.mild, 0..20),
    WARM(R.string.warm, 21..40),
    HOT(R.string.hot, 41..60),
    SPICY(R.string.spicy, 61..80),
    BLAZING(R.string.blazing, 81..100);

    companion object {
        fun fromHeat(heat: Int?): HeatBand {
            val h = normalizeHeat(heat)
            return entries.firstOrNull { h in it.range } ?: MILD
        }
    }
}

/**
 * Canonical 0–100 heat. Legacy rows on the 1–5 scale are mapped ×20 so old
 * backends still render correctly (the backend migration does the same).
 */
fun normalizeHeat(heat: Int?): Int {
    val h = heat ?: 0
    return if (h in 1..5) h * 20 else h.coerceIn(0, 100)
}

/**
 * Heat meter: gradient bar (mint → gold → ember → hot pink) filled to [heat],
 * 5 band ticks and the current band label. Animates in on first composition.
 */
@Composable
fun HeatMeter(
    heat: Int?,
    modifier: Modifier = Modifier,
    showBandLabels: Boolean = true,
) {
    val colors = DeepTalkTheme.colors
    val value = normalizeHeat(heat)
    val band = HeatBand.fromHeat(value)
    val bandColor = colors.heatColor(value)
    val reduced = DeepTalkTheme.reducedMotion
    val progress = remember { Animatable(if (reduced) value / 100f else 0f) }
    LaunchedEffect(value) {
        progress.animateTo(
            (value / 100f).coerceAtLeast(0.04f),
            tween(900, delayMillis = 150, easing = Motion.EmphasizedDecelerate)
        )
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = bandColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.heat_level),
                style = DeepTalkTheme.type.title,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            OverlinePill(text = stringResource(band.label), color = bandColor)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .glow(bandColor, radius = 12.dp, alpha = 0.22f, offsetY = 0.dp)
        ) {
            val radius = CornerRadius(size.height / 2, size.height / 2)
            drawRoundRect(color = colors.surfaceHigh, cornerRadius = radius)
            val fillWidth = size.width * progress.value
            drawRoundRect(
                brush = Brush.horizontalGradient(colors.heatStops, startX = 0f, endX = size.width),
                size = Size(fillWidth, size.height),
                cornerRadius = radius
            )
            // Band separators
            for (i in 1 until HeatBand.entries.size) {
                val x = size.width * i / HeatBand.entries.size
                drawLine(
                    color = colors.bgBase.copy(alpha = 0.55f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            // Thumb
            drawCircle(
                color = Color.White,
                radius = size.height * 0.62f,
                center = Offset(fillWidth.coerceIn(size.height / 2, size.width - size.height / 2), size.height / 2)
            )
            drawCircle(
                color = bandColor,
                radius = size.height * 0.36f,
                center = Offset(fillWidth.coerceIn(size.height / 2, size.width - size.height / 2), size.height / 2)
            )
        }
        if (showBandLabels) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                HeatBand.entries.forEach {
                    val active = it == band
                    Text(
                        text = stringResource(it.label),
                        style = DeepTalkTheme.type.overline,
                        color = if (active) bandColor else colors.textMuted
                    )
                }
            }
        }
    }
}

/** Compact heat indicator: flame + band name, for tiles and lists. */
@Composable
fun HeatDot(heat: Int?, modifier: Modifier = Modifier) {
    val colors = DeepTalkTheme.colors
    val value = normalizeHeat(heat)
    val c = colors.heatColor(value)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = c, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(stringResource(HeatBand.fromHeat(value).label), style = DeepTalkTheme.type.label, color = c)
    }
}
