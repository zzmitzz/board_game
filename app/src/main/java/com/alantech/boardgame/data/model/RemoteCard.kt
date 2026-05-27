package com.alantech.boardgame.data.model

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
    @SerialName("updated_at") val updatedAt: String? = null
)
