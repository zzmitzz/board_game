package com.boardgame.deepdeck.features.call

/**
 * LOBBY → JOINING → WAITING (alone in the room) ⇄ CONNECTING (WebRTC negotiating) → IN_CALL.
 * Anything → ENDED. A peer leaving drops back to WAITING: the room stays open for a rejoin.
 */
enum class CallPhase { LOBBY, JOINING, WAITING, CONNECTING, IN_CALL, ENDED }

enum class EndReason { LEFT, ROOM_NOT_FOUND, ROOM_FULL, REPLACED, UNAUTHORIZED, CONNECTION_LOST }

data class PeerUi(
    val peerId: String,
    val name: String,
    val micOn: Boolean = true,
    val cameraOn: Boolean = true,
    /** Their socket dropped; the server holds their seat for a while. */
    val reconnecting: Boolean = false,
)

enum class ChatStatus { SENDING, SENT, FAILED }

data class ChatItem(
    val id: String,
    val mine: Boolean,
    val name: String,
    val text: String,
    val sentAtMillis: Long,
    val status: ChatStatus,
)

data class CallUiState(
    val code: String,
    val isHost: Boolean,
    val phase: CallPhase = CallPhase.LOBBY,
    val displayName: String = "",
    val hasMicPermission: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val micOn: Boolean = true,
    val cameraOn: Boolean = true,
    val frontCamera: Boolean = true,
    val speakerOn: Boolean = true,
    /** Our own signaling socket is reconnecting. */
    val reconnecting: Boolean = false,
    /** The media path dropped (ICE disconnected) and is being restored. */
    val mediaReconnecting: Boolean = false,
    /** Still no media path after a while: likely a network that needs a TURN relay. */
    val connectTrouble: Boolean = false,
    val peer: PeerUi? = null,
    /** SystemClock.elapsedRealtime() when media first connected with the current peer. */
    val callStartedAt: Long? = null,
    val messages: List<ChatItem> = emptyList(),
    val chatOpen: Boolean = false,
    val unread: Int = 0,
    val peerTyping: Boolean = false,
    val endReason: EndReason? = null,
    /** How long the call lasted, shown on the ended screen. */
    val endedCallMillis: Long? = null,
) {
    val inRoom: Boolean
        get() = phase == CallPhase.WAITING || phase == CallPhase.CONNECTING || phase == CallPhase.IN_CALL
}

sealed interface CallEffect {
    data class PeerLeft(val name: String, val timedOut: Boolean) : CallEffect
    data class PeerJoined(val name: String) : CallEffect
    /** A chat message arrived while the chat panel was closed. */
    data class IncomingMessage(val name: String, val text: String) : CallEffect
}
