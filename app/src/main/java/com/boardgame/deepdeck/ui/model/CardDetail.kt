package com.boardgame.deepdeck.ui.model

import kotlinx.serialization.Serializable


@Serializable
data class CardDetail(
    val id: String,
    val category: String,
    val description: String,
    val media: CardDetailMedia,
    val hint: String,
    /** Optional reverse side for double-sided cards (API `back_side`). */
    val backSide: String? = null
)


@Serializable
data class CardDetailMedia(
    val image: String?,
    val video: String?,
)