package com.alantech.boardgame.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VibeCategory (
    @SerialName("id"          ) var id         : String? = null,
    @SerialName("category_en" ) var categoryEn : String? = null,
    @SerialName("order"       ) var order      : Int?    = null
)