package com.boardgame.deepdeck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * "Midnight Velvet" design tokens (UPGRADE_PLAN §2.1).
 * Raw tokens live in [DeepTalkPalette]; screens should read them through
 * `MaterialTheme.colorScheme` or [DeepTalkTheme.colors].
 */
object DeepTalkPalette {
    val BgBase = Color(0xFF0B0612)
    val BgElevated = Color(0xFF140C1F)
    val Surface = Color(0xFF1D1430)
    val SurfaceHigh = Color(0xFF291C41)
    val Outline = Color(0x14FFFFFF) // #FFFFFF @ 8%
    val TextPrimary = Color(0xFFF6F0FF)
    val TextSecondary = Color(0xFFB8A7D3)
    val TextMuted = Color(0xFF7C6B96)
    val Brand = Color(0xFFB57BFF)
    val BrandStrong = Color(0xFF8B3DFF)
    val BrandOn = Color(0xFF1A0633)
    val Mint = Color(0xFF5EE6B0)
    val Rose = Color(0xFFFF5C8A)
    val Ember = Color(0xFFFF7A59)
    val Gold = Color(0xFFFFC76B)
    val Sky = Color(0xFF6FC3FF)

    // Level colors
    val LevelIcebreaker = Sky
    val LevelDeep = Color(0xFF9B6BFF)
    val LevelIntimate = Rose

    // Card face
    val CardFaceTop = Color(0xFF2A1648)
    val CardFaceBottom = Color(0xFF140A22)

    // Heat scale stops 0 → 100
    val HeatStops = listOf(Mint, Gold, Ember, Color(0xFFFF3D6E))
}

@Immutable
data class DeepTalkColors(
    val bgBase: Color = DeepTalkPalette.BgBase,
    val bgElevated: Color = DeepTalkPalette.BgElevated,
    val surface: Color = DeepTalkPalette.Surface,
    val surfaceHigh: Color = DeepTalkPalette.SurfaceHigh,
    val outline: Color = DeepTalkPalette.Outline,
    val textPrimary: Color = DeepTalkPalette.TextPrimary,
    val textSecondary: Color = DeepTalkPalette.TextSecondary,
    val textMuted: Color = DeepTalkPalette.TextMuted,
    val brand: Color = DeepTalkPalette.Brand,
    val brandStrong: Color = DeepTalkPalette.BrandStrong,
    val brandOn: Color = DeepTalkPalette.BrandOn,
    val mint: Color = DeepTalkPalette.Mint,
    val rose: Color = DeepTalkPalette.Rose,
    val ember: Color = DeepTalkPalette.Ember,
    val gold: Color = DeepTalkPalette.Gold,
    val sky: Color = DeepTalkPalette.Sky,
    val levelIcebreaker: Color = DeepTalkPalette.LevelIcebreaker,
    val levelDeep: Color = DeepTalkPalette.LevelDeep,
    val levelIntimate: Color = DeepTalkPalette.LevelIntimate,
    val cardFaceTop: Color = DeepTalkPalette.CardFaceTop,
    val cardFaceBottom: Color = DeepTalkPalette.CardFaceBottom,
    val heatStops: List<Color> = DeepTalkPalette.HeatStops,
) {
    /** Glass fill: surface @ 72%. */
    val glass: Color get() = surface.copy(alpha = 0.72f)

    /** Brand gradient #B57BFF → #8B3DFF (135°). */
    val brandGradient: Brush
        get() = Brush.linearGradient(listOf(brand, brandStrong))

    /** Vertical card-face gradient. */
    val cardFaceGradient: Brush
        get() = Brush.verticalGradient(listOf(cardFaceTop, cardFaceBottom))

    /** Interpolated heat color for a 0–100 value. */
    fun heatColor(heat: Int): Color {
        val t = (heat.coerceIn(0, 100) / 100f) * (heatStops.size - 1)
        val i = t.toInt().coerceAtMost(heatStops.size - 2)
        return lerp(heatStops[i], heatStops[i + 1], t - i)
    }
}

/** Default vibe gradients (plan §2.1). Overridden by `color_start/color_end` from the API. */
object VibeGradients {
    val Friends = Color(0xFF6FC3FF) to Color(0xFF5B5BFF)
    val Party = Color(0xFFFFB84D) to Color(0xFFFF5C8A)
    val Drinking = Color(0xFFFFC76B) to Color(0xFFFF7A59)
    val Love = Color(0xFFFF5C8A) to Color(0xFFB57BFF)
    val Fallback = Color(0xFFB57BFF) to Color(0xFF8B3DFF)
}

/** Parses '#RRGGBB' or '#AARRGGBB'; returns null when blank or malformed. */
fun String?.toComposeColorOrNull(): Color? {
    val raw = this?.trim()?.removePrefix("#") ?: return null
    return try {
        when (raw.length) {
            6 -> Color(("FF$raw").toLong(16))
            8 -> Color(raw.toLong(16))
            else -> null
        }
    } catch (_: NumberFormatException) {
        null
    }
}

// ---------------------------------------------------------------------------
// Legacy names kept so untouched screens (gameplay, library) pick up the new
// palette without edits. Prefer DeepTalkTheme.colors in new code.
// ---------------------------------------------------------------------------
val LightBackground = DeepTalkPalette.BgBase
val LightTextOnBackground = DeepTalkPalette.TextPrimary
val LightSecondTextOBG = DeepTalkPalette.Brand
val LightPrimary = DeepTalkPalette.Surface
val LightOnPrimary = DeepTalkPalette.TextPrimary
val LightDialogBackground = DeepTalkPalette.SurfaceHigh
val LightTextColor = DeepTalkPalette.TextPrimary
