package com.boardgame.deepdeck.data.model

import com.boardgame.deepdeck.ui.model.PackDetailUIModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotePackDetail(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("thumb") val thumb: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("estimate_time_play") val estimateTimePlay: Int? = null,
    @SerialName("suggest_number_players") val suggestNumberPlayers: Int? = null,
    @SerialName("keywords_summarise") val tag: String? = null,
    @SerialName("heat_level") val heatLevel: Int? = null,
    @SerialName("total_cards") val totalCards: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("how_to_play") val howToPlay: String? = null,
    @SerialName("is_premium") val isPremium: Boolean? = null,
    @SerialName("accent_color") val accentColor: String? = null,
    @SerialName("play_count") val playCount: Int? = null,
    @SerialName("badge") val badge: String? = null
)

@Serializable
data class PacksPreview(
    @SerialName("id"                 ) var id                : String? = null,
    @SerialName("title"              ) var title             : String? = null,
    @SerialName("keywords_summarise" ) var keywordsSummarise : String? = null,
    @SerialName("thumb"              ) var thumb             : String? = null,
    @SerialName("cover_image_url"    ) var coverImageUrl     : String? = null,
    @SerialName("heat_level"         ) var heatLevel         : Int? = null,
    @SerialName("total_cards"        ) var totalCards        : Int? = null,
    @SerialName("is_premium"         ) var isPremium         : Boolean? = null,
    @SerialName("accent_color"       ) var accentColor       : String? = null,
    @SerialName("badge"              ) var badge             : String? = null
)

/** Best image for a tile: thumb first, then cover. */
val PacksPreview.tileImage: String?
    get() = thumb?.takeIf { it.isNotBlank() } ?: coverImageUrl?.takeIf { it.isNotBlank() }


fun RemotePackDetail.toUIModel(): PackDetailUIModel {
    return PackDetailUIModel(
        id = id.orEmpty(),
        thumbnail = thumb ?: coverImageUrl.orEmpty(),
        titleCard = title.orEmpty(),
        creator = tag ?: "Premium",
        description = description,
        coverImageUrl = coverImageUrl,
        thumb = thumb,
        estimateTimePlay = estimateTimePlay,
        suggestNumberPlayers = suggestNumberPlayers,
        tag = tag,
        heatLevel = heatLevel,
        totalCards = totalCards,
        howToPlay = howToPlay?.takeIf { it.isNotBlank() },
        isPremium = isPremium == true,
        accentColor = accentColor,
        badge = badge
    )
}