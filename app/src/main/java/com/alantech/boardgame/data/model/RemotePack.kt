package com.alantech.boardgame.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotePack(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("thumb") val thumb: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("estimate_time_play") val estimateTimePlay: Int? = null,
    @SerialName("suggest_number_players") val suggestNumberPlayers: Int? = null,
    @SerialName("tag") val tag: String? = null,
    @SerialName("heat_level") val heatLevel: Int? = null,
    @SerialName("total_cards") val totalCards: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("how_to_play") val howToPlay: String? = null
)
