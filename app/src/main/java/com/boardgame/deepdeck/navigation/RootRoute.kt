package com.boardgame.deepdeck.navigation

import kotlinx.serialization.Serializable

sealed class RootRoute {
    @Serializable data object Onboarding : RootRoute()

    /** Play tab graph (Home → vibe/section/pack → setup → game → end). */
    @Serializable data object Home : RootRoute()

    /** Library tab root. */
    @Serializable data object Library : RootRoute()

    /** You tab root (stats + settings). */
    @Serializable data object You : RootRoute()

    /**
     * Immersive game flow (Setup → Play → End) as one nested graph; the game ViewModels are
     * scoped to this graph entry. [isCustom] = local My Library pack; [quickPlay] = one-tap
     * "Play again" (roster pre-filled, start focused).
     */
    @Serializable data class GameFlow(
        val packId: String,
        val isCustom: Boolean = false,
        val quickPlay: Boolean = false,
    ) : RootRoute()

    @Serializable data object Language : RootRoute()
    @Serializable data object MyLibrary : RootRoute()
}
