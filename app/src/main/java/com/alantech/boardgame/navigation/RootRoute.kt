package com.alantech.boardgame.navigation

import kotlinx.serialization.Serializable

sealed class RootRoute {
    @Serializable data object Onboarding : RootRoute()
    @Serializable data object Home : RootRoute()
    @Serializable data object Setting : RootRoute()
}