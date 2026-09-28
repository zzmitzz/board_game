package com.boardgame.deepdeck.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class VibeCategory (
    @SerialName("id"          ) var id         : String? = null,
    @SerialName("category_en" ) var categoryEn : String? = null,
    @SerialName("order"       ) var order      : Int?    = null,
    /** Localized by `?lang=`, falls back to `category_en` server-side. */
    @SerialName("name"        ) var name       : String? = null,
    /** party | friends | drinking | love | family | work | couple | solo */
    @SerialName("icon_key"    ) var iconKey    : String? = null,
    @SerialName("color_start" ) var colorStart : String? = null,
    @SerialName("color_end"   ) var colorEnd   : String? = null,
    @SerialName("description" ) var description: String? = null,
    @SerialName("translations") var translations: JsonObject? = null,
    @SerialName("pack_count"  ) var packCount  : Int?    = null,
)
