package com.boardgame.deepdeck.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteRoomCreated(
    val code: String,
    val capacity: Int = 2,
    @SerialName("expires_in_seconds") val expiresInSeconds: Int = 0,
)

@Serializable
data class RemoteRoomInfo(
    val code: String,
    val capacity: Int = 2,
    val participants: Int = 0,
    val joinable: Boolean = false,
)
