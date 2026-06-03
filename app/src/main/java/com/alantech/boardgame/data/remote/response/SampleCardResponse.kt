package com.alantech.boardgame.data.remote.response

import com.alantech.boardgame.data.model.RemoteCard
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SampleCardResponse(
    @SerialName("success") val isSuccess: Boolean,
    @SerialName("cards") val cards: List<RemoteCard>
)