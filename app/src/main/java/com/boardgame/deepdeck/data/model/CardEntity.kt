package com.boardgame.deepdeck.data.model

import com.boardgame.deepdeck.ui.model.CardAsset
import com.boardgame.deepdeck.ui.model.CardAssetKind
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.CardDetailMedia
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCard(
    @SerialName("id") val id: String? = null,
    @SerialName("pack_id") val packId: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("level") val level: String? = null,
    @SerialName("locale") val locale: String? = null,
    @SerialName("front_side") val frontSide: String? = null,
    @SerialName("back_side") val backSide: String? = null,
    @SerialName("hint") val hint: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("assets") val assets: List<RemoteCardAsset> = emptyList()
)

/** One entry of a card's `assets` (backend `CardAssetResponse`). */
@Serializable
data class RemoteCardAsset(
    @SerialName("id") val id: String? = null,
    @SerialName("kind") val kind: String? = null,
    @SerialName("placement") val placement: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("caption") val caption: String? = null,
    @SerialName("alt_text") val altText: String? = null,
    @SerialName("display_order") val displayOrder: Int? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
)

fun RemoteCardAsset.toUIModel(): CardAsset? {
    val assetKind = CardAssetKind.entries.firstOrNull { it.name == kind } ?: return null
    val assetUrl = url?.takeIf { it.isNotBlank() } ?: return null
    return CardAsset(
        id = id ?: assetUrl,
        kind = assetKind,
        url = assetUrl,
        onBack = placement == "BACK",
        isBackground = role == "BACKGROUND",
        thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
        title = title?.takeIf { it.isNotBlank() },
        caption = caption?.takeIf { it.isNotBlank() },
        altText = altText?.takeIf { it.isNotBlank() },
    )
}

/** Active assets in display order, mapped to the UI model (unknown kinds are skipped). */
fun RemoteCard.assetsUIModel(): List<CardAsset> =
    assets.filter { it.isActive != false }
        .sortedBy { it.displayOrder ?: 0 }
        .mapNotNull { it.toUIModel() }

fun RemoteCard.toUIModel(): CardDetail {
    val cardAssets = assetsUIModel()
    val front = cardAssets.filter { !it.onBack }
    return CardDetail(
        id = id.orEmpty(),
        category = normalizeLevel(level) ?: type.orEmpty(),
        description = frontSide.orEmpty(),
        media = CardDetailMedia(
            image = front.firstOrNull { it.kind == CardAssetKind.IMAGE }?.url,
            video = front.firstOrNull { it.kind == CardAssetKind.VIDEO }?.url
        ),
        hint = hint.orEmpty(),
        backSide = backSide?.takeIf { it.isNotBlank() },
        assets = cardAssets
    )
}
