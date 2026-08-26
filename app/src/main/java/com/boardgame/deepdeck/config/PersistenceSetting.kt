package com.boardgame.deepdeck.config

import kotlinx.serialization.Serializable

@Serializable
data class PersistenceSetting(
    val isAutoTranslate: Boolean = false,
    val isHapticOn: Boolean = false,
    val isSoundOn: Boolean = false,
    val language: String = "en"
)
