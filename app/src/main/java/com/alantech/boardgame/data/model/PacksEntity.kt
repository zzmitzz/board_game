package com.alantech.boardgame.data.model

import com.alantech.boardgame.ui.model.PackDetailUIModel
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
    @SerialName("how_to_play") val howToPlay: String? = null
)

@Serializable
data class PacksPreview(
    @SerialName("id"                 ) var id                : String? = null,
    @SerialName("title"              ) var title             : String? = null,
    @SerialName("keywords_summarise" ) var keywordsSummarise : String? = null,
    @SerialName("thumb"              ) var thumb             : String? = null
)


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
        howToPlay = howToPlay
    )
}