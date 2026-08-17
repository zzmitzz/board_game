package com.alantech.boardgame.data.remote.request

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CardTranslateRequest(
    @SerialName("card_ids") val card_ids: List<String>,
    @SerialName("locale") val locale: String = "en"
)