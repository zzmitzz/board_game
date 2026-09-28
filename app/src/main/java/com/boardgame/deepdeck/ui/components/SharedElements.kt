package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.boardgame.deepdeck.ui.theme.LocalReducedMotion

/** Provided by NavigationGraph (wraps the NavHost in SharedTransitionLayout). */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** Provided per destination (the destination's AnimatedContentScope). */
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Wraps a destination's content so shared elements inside can find their scope. */
@Composable
fun ProvideNavAnimatedScope(scope: AnimatedVisibilityScope, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides scope, content = content)
}

/** Shared-element key for a pack cover coming from a given surface (section, vibe…). */
fun packCoverKey(source: String, packId: String): String = "pack-cover/$source/$packId"

/**
 * Shared-element modifier for pack covers (tile → detail). No-op when [key] is
 * null, no scopes are available, or reduced motion is on.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedPackCover(
    key: String?,
    clipShape: Shape = RoundedCornerShape(0),
): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalNavAnimatedVisibilityScope.current
    if (key == null || sharedScope == null || animatedScope == null || LocalReducedMotion.current) return this
    return with(sharedScope) {
        this@sharedPackCover.sharedElement(
            rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedScope,
            clipInOverlayDuringTransition = OverlayClip(clipShape),
        )
    }
}
