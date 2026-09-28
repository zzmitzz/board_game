package com.boardgame.deepdeck.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalDeepTalkColors = staticCompositionLocalOf { DeepTalkColors() }
val LocalDeepTalkTypography = staticCompositionLocalOf { DeepTalkTypography() }

/** True when system animations are off; flips/swipes should become crossfades. */
val LocalReducedMotion = staticCompositionLocalOf { false }

/** Accessors for DeepTalk tokens that Material's scheme doesn't cover. */
object DeepTalkTheme {
    val colors: DeepTalkColors
        @Composable @ReadOnlyComposable get() = LocalDeepTalkColors.current

    val type: DeepTalkTypography
        @Composable @ReadOnlyComposable get() = LocalDeepTalkTypography.current

    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReducedMotion.current
}

private fun midnightVelvetScheme(c: DeepTalkColors) = darkColorScheme(
    primary = c.brand,
    onPrimary = c.brandOn,
    primaryContainer = c.brandStrong,
    onPrimaryContainer = c.textPrimary,
    inversePrimary = c.brandStrong,
    secondary = c.textSecondary,
    onSecondary = c.bgBase,
    secondaryContainer = c.surfaceHigh,
    onSecondaryContainer = c.textPrimary,
    tertiary = c.gold,
    onTertiary = c.brandOn,
    tertiaryContainer = c.surfaceHigh,
    onTertiaryContainer = c.gold,
    background = c.bgBase,
    onBackground = c.textPrimary,
    surface = c.bgElevated,
    onSurface = c.textPrimary,
    surfaceVariant = c.surface,
    onSurfaceVariant = c.textSecondary,
    surfaceTint = c.brand,
    inverseSurface = c.textPrimary,
    inverseOnSurface = c.bgBase,
    error = c.rose,
    onError = c.brandOn,
    errorContainer = c.rose.copy(alpha = 0.2f),
    onErrorContainer = c.rose,
    outline = Color(0x29FFFFFF),
    outlineVariant = c.outline,
    scrim = Color(0xCC05020A),
    surfaceBright = c.surfaceHigh,
    surfaceDim = c.bgBase,
    surfaceContainerLowest = c.bgBase,
    surfaceContainerLow = c.bgElevated,
    surfaceContainer = c.bgElevated,
    surfaceContainerHigh = c.surface,
    surfaceContainerHighest = c.surfaceHigh,
)

/**
 * DeepTalk "Midnight Velvet" theme. Dark only by design.
 * Provides Material3 color/type/shape plus [LocalDeepTalkColors],
 * [LocalDeepTalkTypography] and [LocalReducedMotion].
 */
@Composable
fun BoardGameTheme(content: @Composable () -> Unit) {
    val colors = remember { DeepTalkColors() }
    val type = remember { DeepTalkTypography() }
    val context = LocalContext.current
    val reducedMotion = remember(context) { context.isReducedMotionEnabled() }
    CompositionLocalProvider(
        LocalDeepTalkColors provides colors,
        LocalDeepTalkTypography provides type,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            colorScheme = remember(colors) { midnightVelvetScheme(colors) },
            typography = Typography,
            shapes = MaterialShapes,
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.textPrimary) {
                content()
            }
        }
    }
}
