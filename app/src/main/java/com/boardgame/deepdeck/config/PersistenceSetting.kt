package com.boardgame.deepdeck.config

import kotlinx.serialization.Serializable

@Serializable
data class PersistenceSetting(
    val isAutoTranslate: Boolean = false,
    val isHapticOn: Boolean = true,
    val isSoundOn: Boolean = true,
    val language: String = "en"
)
