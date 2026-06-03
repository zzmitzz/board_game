package com.alantech.boardgame.data.remote.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SampleCardRequest(
    @SerialName("pack_id") val packId: String
)