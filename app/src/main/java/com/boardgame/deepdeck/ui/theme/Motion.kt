package com.boardgame.deepdeck.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for the motion language (plan §2.4).
 * Durations are in milliseconds.
 */
object Motion {
    // Durations
    const val SCREEN = 300
    const val LIST_ENTRANCE = 280
    const val STAGGER_STEP = 40L
    const val STAGGER_MAX_ITEMS = 8
    const val FLIP = 420
    const val PASS_REVEAL = 450
    const val SHIMMER_PERIOD = 3000
    const val SKELETON_PERIOD = 1200

    // Easings
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Flip: Easing = FastOutSlowInEasing

    // Springs
    /** Press feedback: scale 0.96, dampingRatio 0.6, stiffness 800. */
    const val PRESS_SCALE = 0.96f
    fun <T> pressSpring(): SpringSpec<T> = spring(dampingRatio = 0.6f, stiffness = 800f)

    /** Card swipe spring-back (dampingRatio 0.55). */
    fun <T> swipeBackSpring(): SpringSpec<T> =
        spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)

    /** Next card rising from the stack (medium-bouncy). */
    fun <T> nextCardSpring(): SpringSpec<T> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

    /** Streak flame pop 1 → 1.25 → 1. */
    fun <T> popSpring(): SpringSpec<T> =
        spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium)

    // Card swipe gesture constants
    const val SWIPE_MAX_ROTATION_DEG = 14f
    const val SWIPE_COMMIT_FRACTION = 0.35f
    const val SWIPE_COMMIT_VELOCITY_DP = 1200f
    const val CARD_CAMERA_DISTANCE = 12f

    // Distances
    val SharedAxisOffset = 30.dp
    val EntranceOffset = 16.dp

    // -----------------------------------------------------------------------
    // Navigation: shared axis X (slide 30dp + fade, 300ms)
    // -----------------------------------------------------------------------
    fun sharedAxisEnter(offsetPx: Int, forward: Boolean = true): EnterTransition =
        slideInHorizontally(tween(SCREEN, easing = EmphasizedDecelerate)) {
            if (forward) offsetPx else -offsetPx
        } + fadeIn(tween(SCREEN - 90, delayMillis = 90, easing = EmphasizedDecelerate))

    fun sharedAxisExit(offsetPx: Int, forward: Boolean = true): ExitTransition =
        slideOutHorizontally(tween(SCREEN, easing = EmphasizedAccelerate)) {
            if (forward) -offsetPx else offsetPx
        } + fadeOut(tween(90, easing = EmphasizedAccelerate))

    /** Fade-through used between bottom-nav tabs and under reduced motion. */
    fun fadeThroughEnter(): EnterTransition =
        fadeIn(tween(210, delayMillis = 90, easing = EmphasizedDecelerate))

    fun fadeThroughExit(): ExitTransition = fadeOut(tween(90, easing = EmphasizedAccelerate))
}

/** True when the user disabled animations (Settings › Developer › Animator duration scale = 0). */
fun Context.isReducedMotionEnabled(): Boolean = try {
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
} catch (_: Exception) {
    false
}
