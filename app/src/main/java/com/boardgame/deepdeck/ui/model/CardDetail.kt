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
    val backSide: String? = null,
    /** Active server assets (API `assets`), already sorted by `display_order`. */
    val assets: List<CardAsset> = emptyList()
)


@Serializable
data class CardDetailMedia(
    val image: String?,
    val video: String?,
)

enum class CardAssetKind { IMAGE, VIDEO, AUDIO, LINK, EMBED }

@Serializable
data class CardAsset(
    val id: String,
    val kind: CardAssetKind,
    val url: String,
    /** True for the back side of a double-sided card. */
    val onBack: Boolean = false,
    /** True when the asset fills the card behind the text (API role `BACKGROUND`). */
    val isBackground: Boolean = false,
    val thumbnailUrl: String? = null,
    val title: String? = null,
    val caption: String? = null,
    val altText: String? = null,
)

/** What one card face shows besides its text. */
data class CardFaceMedia(
    val background: CardAsset? = null,
    val inline: List<CardAsset> = emptyList(),
) {
    val isEmpty: Boolean get() = background == null && inline.isEmpty()
}

/** Splits [assets] for one side; a background is only IMAGE / VIDEO, as the server enforces. */
fun List<CardAsset>.faceMedia(back: Boolean): CardFaceMedia {
    val side = filter { it.onBack == back }
    return CardFaceMedia(
        background = side.firstOrNull { it.isBackground },
        inline = side.filter { !it.isBackground }
    )
}

/** Front media; custom (local) cards only carry an image URI in [CardDetail.media]. */
fun CardDetail.frontMedia(): CardFaceMedia {
    val remote = assets.faceMedia(back = false)
    val localImage = media.image?.takeIf { it.isNotBlank() }
    if (!remote.isEmpty || localImage == null) return remote
    return CardFaceMedia(inline = listOf(CardAsset(id = "local-$id", kind = CardAssetKind.IMAGE, url = localImage)))
}

fun CardDetail.backMedia(): CardFaceMedia = assets.faceMedia(back = true)
