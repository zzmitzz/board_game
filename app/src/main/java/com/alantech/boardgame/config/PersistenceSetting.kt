package com.alantech.boardgame.config

data class PersistenceSetting(
    val isAutoTranslate: Boolean = false,
    val language: String = "en"
)

val listLanguageSupport = listOf("en", "vi")