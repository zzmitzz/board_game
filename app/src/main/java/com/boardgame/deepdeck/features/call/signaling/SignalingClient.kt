package com.boardgame.deepdeck.features.call.signaling

import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.random.Random

/**
 * One phone's connection to a call room: an OkHttp WebSocket that reconnects by itself.
 *
 * A dropped socket (network switch, tunnel, elevator) is reopened with backoff and the same
 * peer id, so the server gives back the same seat (`joined.resumed = true`) and the other side
 * only sees "reconnecting". Only room-level answers (gone, full, replaced) end the connection.
 * The X-API-Key header comes from the shared OkHttpClient's interceptor (NetworkModule).
 */
class SignalingClient(
    baseClient: OkHttpClient,
    private val json: Json,
    private val scope: CoroutineScope,
    private val baseUrl: String,
    private val roomCode: String,
    private val deviceId: String,
    private val displayName: String,
) {
    sealed interface State {
        data object Idle : State
        /** [attempt] 0 = first connection, 1+ = reconnecting. */
        data class Connecting(val attempt: Int) : State
        /** The server seated us (the `joined` frame arrived). */
        data object Open : State
        data class Closed(val reason: CloseReason) : State
    }

    enum class CloseReason { LEFT, ROOM_NOT_FOUND, ROOM_FULL, REPLACED, UNAUTHORIZED, GAVE_UP }

    // WebSocket-level pings every 15 s: a dead connection fails within ~15 s instead of hanging.
    private val client = baseClient.newBuilder()
        .pingInterval(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _frames = MutableSharedFlow<ServerFrame>(extraBufferCapacity = 256)
    val frames: SharedFlow<ServerFrame> = _frames.asSharedFlow()

    /** Our seat in the room; sent again on reconnect to resume it. */
    @Volatile var peerId: String? = null
        private set

    @Volatile private var socket: WebSocket? = null
    @Volatile private var closedByUs = false
    private var attempt = 0
    private var lostSince: Long? = null
    private var reconnectJob: Job? = null

    fun connect() {
        closedByUs = false
        open()
    }

    /** Network came back: skip the rest of the backoff. */
    fun reconnectNow() {
        if (closedByUs || _state.value !is State.Connecting || socket != null) return
        reconnectJob?.cancel()
        open()
    }

    fun send(frame: ClientFrame): Boolean {
        val ws = socket ?: return false
        if (_state.value != State.Open) return false
        return ws.send(json.encodeToString(ClientFrame.serializer(), frame))
    }

    /** Hang up: frees the seat at once (no grace period) and stops reconnecting. */
    fun leave() {
        closedByUs = true
        reconnectJob?.cancel()
        socket?.let { ws ->
            ws.send(json.encodeToString(ClientFrame.serializer(), ClientFrame.Leave))
            ws.close(NORMAL_CLOSURE, "leave")
        }
        socket = null
        _state.value = State.Closed(CloseReason.LEFT)
    }

    @Synchronized
    private fun open() {
        if (closedByUs) return
        _state.value = State.Connecting(attempt)
        val url = baseUrl.toHttpUrl().newBuilder()
            .addPathSegments("api/v1/calls/rooms")
            .addPathSegment(roomCode)
            .addPathSegment("ws")
            .addQueryParameter("device_id", deviceId)
            .addQueryParameter("name", displayName)
            .apply { peerId?.let { addQueryParameter("peer_id", it) } }
            .build()
        socket = client.newWebSocket(Request.Builder().url(url).build(), Listener())
    }

    @Synchronized
    private fun scheduleReconnect() {
        socket = null
        if (closedByUs) return
        val now = SystemClock.elapsedRealtime()
        val since = lostSince ?: now.also { lostSince = it }
        // Never seated (server down, wrong address): fail in seconds, not after the full window.
        if (now - since > GIVE_UP_AFTER_MS || (peerId == null && attempt >= FIRST_CONNECT_ATTEMPTS)) {
            _state.value = State.Closed(CloseReason.GAVE_UP)
            return
        }
        attempt++
        // 0.5 s, 1 s, 2 s, 4 s, then every 8 s, with jitter so both phones don't retry in lockstep.
        val backoff = min(MAX_BACKOFF_MS, BASE_BACKOFF_MS shl min(attempt - 1, 4)) + Random.nextLong(250)
        _state.value = State.Connecting(attempt)
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(backoff)
            open()
        }
    }

    private fun closeFor(code: Int): CloseReason? = when (code) {
        CLOSE_ROOM_NOT_FOUND -> CloseReason.ROOM_NOT_FOUND
        CLOSE_ROOM_FULL -> CloseReason.ROOM_FULL
        CLOSE_REPLACED -> CloseReason.REPLACED
        CLOSE_UNAUTHORIZED -> CloseReason.UNAUTHORIZED
        else -> null
    }

    private inner class Listener : WebSocketListener() {

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (webSocket !== socket) return
            val frame = try {
                json.decodeFromString(ServerFrame.serializer(), text)
            } catch (e: SerializationException) {
                Log.w(TAG, "Ignoring unknown frame: ${e.message}")
                return
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Ignoring malformed frame: ${e.message}")
                return
            }
            if (frame is ServerFrame.Joined) {
                peerId = frame.peerId
                attempt = 0
                lostSince = null
                _state.value = State.Open
            }
            _frames.tryEmit(frame)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NORMAL_CLOSURE, null)
            onEnded(webSocket, code)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) = onEnded(webSocket, code)

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (webSocket !== socket) return
            Log.i(TAG, "Socket failed (${response?.code}): ${t.message}")
            if (response?.code == 403) {
                socket = null
                _state.value = State.Closed(CloseReason.UNAUTHORIZED)
            } else {
                scheduleReconnect()
            }
        }

        private fun onEnded(webSocket: WebSocket, code: Int) {
            if (webSocket !== socket) return
            val terminal = closeFor(code)
            if (terminal != null) {
                socket = null
                closedByUs = true
                _state.value = State.Closed(terminal)
            } else {
                scheduleReconnect()
            }
        }
    }

    private companion object {
        const val TAG = "SignalingClient"
        const val NORMAL_CLOSURE = 1000
        const val CLOSE_REPLACED = 4000
        const val CLOSE_UNAUTHORIZED = 4401
        const val CLOSE_ROOM_NOT_FOUND = 4404
        const val CLOSE_ROOM_FULL = 4409
        const val BASE_BACKOFF_MS = 500L
        const val MAX_BACKOFF_MS = 8_000L
        /** Past the server's 30 s grace the seat is gone; keep trying a while for a fresh seat. */
        const val GIVE_UP_AFTER_MS = 90_000L
        const val FIRST_CONNECT_ATTEMPTS = 4
    }
}
