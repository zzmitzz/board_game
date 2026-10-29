package com.boardgame.deepdeck.features.call.signaling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * The room WebSocket protocol (backend: docs/live-call.md). Every frame is one JSON object whose
 * "type" picks the class below (kotlinx's default class discriminator is "type").
 */

@Serializable
data class PeerInfo(
    @SerialName("peer_id") val peerId: String,
    val name: String,
    val audio: Boolean = true,
    val video: Boolean = true,
    val connected: Boolean = true,
)

@Serializable
data class IceServerInfo(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null,
)

/** Server → app. */
@Serializable
sealed class ServerFrame {

    @Serializable @SerialName("joined")
    data class Joined(
        val room: String,
        @SerialName("peer_id") val peerId: String,
        val resumed: Boolean = false,
        val capacity: Int = 2,
        val peers: List<PeerInfo> = emptyList(),
        val history: List<Chat> = emptyList(),
        @SerialName("ice_servers") val iceServers: List<IceServerInfo> = emptyList(),
    ) : ServerFrame()

    @Serializable @SerialName("peer_joined")
    data class PeerJoined(val peer: PeerInfo) : ServerFrame()

    /** The other phone's socket dropped; the server holds its seat for ~30 s. */
    @Serializable @SerialName("peer_reconnecting")
    data class PeerReconnecting(@SerialName("peer_id") val peerId: String) : ServerFrame()

    @Serializable @SerialName("peer_resumed")
    data class PeerResumed(@SerialName("peer_id") val peerId: String) : ServerFrame()

    /** [reason]: left · timeout · replaced. */
    @Serializable @SerialName("peer_left")
    data class PeerLeft(@SerialName("peer_id") val peerId: String, val reason: String = "left") : ServerFrame()

    @Serializable @SerialName("offer")
    data class Offer(val sdp: String, @SerialName("from") val fromPeer: String) : ServerFrame()

    @Serializable @SerialName("answer")
    data class Answer(val sdp: String, @SerialName("from") val fromPeer: String) : ServerFrame()

    @Serializable @SerialName("ice")
    data class Ice(
        val candidate: String,
        @SerialName("sdp_mid") val sdpMid: String? = null,
        @SerialName("sdp_mline_index") val sdpMLineIndex: Int? = null,
        @SerialName("from") val fromPeer: String,
    ) : ServerFrame()

    /** The answerer lost the media path and asks us (the offerer) for an ICE restart. */
    @Serializable @SerialName("renegotiate")
    data class Renegotiate(@SerialName("from") val fromPeer: String) : ServerFrame()

    @Serializable @SerialName("media_state")
    data class MediaState(val audio: Boolean, val video: Boolean, @SerialName("from") val fromPeer: String) : ServerFrame()

    @Serializable @SerialName("typing")
    data class Typing(val typing: Boolean, @SerialName("from") val fromPeer: String) : ServerFrame()

    @Serializable @SerialName("chat")
    data class Chat(
        val id: String,
        @SerialName("from") val fromPeer: String,
        val name: String,
        val text: String,
        @SerialName("sent_at") val sentAt: String,
    ) : ServerFrame()

    @Serializable @SerialName("pong")
    data class Pong(val at: String? = null) : ServerFrame()

    /** [ref] = the chat id the error is about, when there is one. */
    @Serializable @SerialName("error")
    data class Error(val error: String, val message: String = "", val ref: String? = null) : ServerFrame()
}

/** App → server. */
@Serializable
sealed class ClientFrame {

    @Serializable @SerialName("offer")
    data class Offer(val sdp: String) : ClientFrame()

    @Serializable @SerialName("answer")
    data class Answer(val sdp: String) : ClientFrame()

    @Serializable @SerialName("ice")
    data class Ice(
        val candidate: String,
        @SerialName("sdp_mid") val sdpMid: String?,
        @SerialName("sdp_mline_index") val sdpMLineIndex: Int,
    ) : ClientFrame()

    @Serializable @SerialName("renegotiate")
    data object Renegotiate : ClientFrame()

    @Serializable @SerialName("media_state")
    data class MediaState(val audio: Boolean, val video: Boolean) : ClientFrame()

    @Serializable @SerialName("typing")
    data class Typing(val typing: Boolean) : ClientFrame()

    @Serializable @SerialName("chat")
    data class Chat(val id: String, val text: String) : ClientFrame()

    @Serializable @SerialName("ping")
    data object Ping : ClientFrame()

    @Serializable @SerialName("leave")
    data object Leave : ClientFrame()
}
