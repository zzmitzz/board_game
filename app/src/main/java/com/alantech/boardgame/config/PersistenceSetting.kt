package com.alantech.boardgame.config

data class PersistenceSetting(
    val isAutoTranslate: Boolean = false,
    val isHapticOn: Boolean = false,
    val isSoundOn: Boolean = false,
    val language: String = listLanguageCodeSupport[0]
)

val listLanguageCodeSupport = listOf("en", "hi", "es", "fr", "id", "tr", "de", "it", "ja", "ko", "pt", "ru", "vi")