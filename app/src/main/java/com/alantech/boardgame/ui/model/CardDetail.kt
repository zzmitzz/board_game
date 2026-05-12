package com.alantech.boardgame.ui.model

import kotlinx.serialization.Serializable


@Serializable
data class CardDetail(
    val id: String,
    val category: String,
    val description: String,
    val media: CardDetailMedia,
)


@Serializable
data class CardDetailMedia(
    val image: String?,
    val video: String?,
)