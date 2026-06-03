package com.alantech.boardgame.ui.model


data class CardPreview(
    val id: String,
    val thumbnail: String,
    val titleCard: String,
    val creator: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val thumb: String? = null,
    val estimateTimePlay: Int? = null,
    val suggestNumberPlayers: Int? = null,
    val tag: String? = null,
    val heatLevel: Int? = null,
    val totalCards: Int? = null,
    val howToPlay: String? = null
)