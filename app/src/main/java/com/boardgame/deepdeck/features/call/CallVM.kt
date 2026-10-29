package com.boardgame.deepdeck.features.call

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.boardgame.deepdeck.BuildConfig
import com.boardgame.deepdeck.data.prefs.CallPrefsRepository
import com.boardgame.deepdeck.data.prefs.DeviceIdRepository
import com.boardgame.deepdeck.features.call.rtc.CallAudio
import com.boardgame.deepdeck.features.call.rtc.RtcException
import com.boardgame.deepdeck.features.call.rtc.RtcSession
import com.boardgame.deepdeck.features.call.signaling.ClientFrame
import com.boardgame.deepdeck.features.call.signaling.IceServerInfo
import com.boardgame.deepdeck.features.call.signaling.PeerInfo
import com.boardgame.deepdeck.features.call.signaling.ServerFrame
import com.boardgame.deepdeck.features.call.signaling.SignalingClient
import com.boardgame.deepdeck.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.VideoTrack
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * One live call room: the lobby preview, the WebSocket signaling, the WebRTC connection and the
 * chat. Scoped to the Room nav entry, so leaving the screen hangs up.
 *
 * Negotiation rule (same as the server's docs): the phone that joins second is the offerer and
 * does every ICE restart; the one already waiting answers and asks for a restart with
 * `renegotiate` when its media path fails.
 */
@HiltViewModel
class CallVM @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val deviceIds: DeviceIdRepository,
    private val callPrefs: CallPrefsRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CallRoute.Room>()
    private val code = route.code.trim().uppercase()

    private val _uiState = MutableStateFlow(CallUiState(code = code, isHost = route.host))
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<CallEffect>(extraBufferCapacity = 8)
    val effects: SharedFlow<CallEffect> = _effects.asSharedFlow()

    private val eglBase: EglBase = EglBase.create()
    /** Shared GL context: the camera, the encoder/decoder and the on-screen renderers. */
    val eglContext: EglBase.Context get() = eglBase.eglBaseContext
    private val rtc = RtcSession(context, eglBase, viewModelScope)
    val localVideo: StateFlow<VideoTrack?> = rtc.localVideo
    val remoteVideo: StateFlow<VideoTrack?> = rtc.remoteVideo
    private val audio = CallAudio(context)

    private var signaling: SignalingClient? = null
    private var sessionJob: Job? = null
    private var iceServers: List<IceServerInfo> = emptyList()
    private var offerer = false
    /** Offers/answers must not interleave (an ICE restart racing an incoming answer). */
    private val negotiation = Mutex()
    private val myMessageIds = mutableSetOf<String>()
    private var iceWatchdog: Job? = null
    private var troubleJob: Job? = null
    private var typingClear: Job? = null
    private var typingSent = false
    private var lastTypingAt = 0L
    private var lastRestartAt = 0L
    private var stopMediaJob: Job? = null

    init {
        viewModelScope.launch { _uiState.update { it.copy(displayName = callPrefs.displayName()) } }
        viewModelScope.launch { rtc.frontCamera.collect { front -> _uiState.update { it.copy(frontCamera = front) } } }
        viewModelScope.launch {
            CallService.hangUpRequests.collect { if (_uiState.value.inRoom) leave() }
        }
    }

    // -- lobby -------------------------------------------------------------------------------

    fun onPermissions(mic: Boolean, camera: Boolean) {
        _uiState.update {
            it.copy(
                hasMicPermission = mic,
                hasCameraPermission = camera,
                micOn = it.micOn && mic,
                cameraOn = it.cameraOn && camera,
            )
        }
        val state = _uiState.value
        if (state.phase == CallPhase.ENDED) return
        rtc.startLocalMedia(withAudio = mic, withVideo = camera)
        rtc.setMicEnabled(state.micOn)
        rtc.setCameraEnabled(state.cameraOn)
    }

    fun setDisplayName(name: String) {
        _uiState.update { it.copy(displayName = name.take(MAX_NAME_CHARS)) }
    }

    /** The lobby preview pauses in the background (no foreground service before joining). */
    fun onScreenVisible(visible: Boolean) {
        val state = _uiState.value
        if (state.phase == CallPhase.LOBBY && state.hasCameraPermission) {
            rtc.setCameraEnabled(visible && state.cameraOn)
        }
    }

    fun join() {
        val state = _uiState.value
        if (state.phase != CallPhase.LOBBY && state.phase != CallPhase.ENDED) return
        val name = state.displayName.trim()
        _uiState.update { it.copy(phase = CallPhase.JOINING, endReason = null, reconnecting = false) }
        viewModelScope.launch {
            callPrefs.saveDisplayName(name)
            val deviceId = deviceIds.get()
            // Backed out while we were getting ready.
            if (_uiState.value.phase != CallPhase.JOINING) return@launch
            stopMediaJob?.join() // a retry right after the last call ended
            rtc.startLocalMedia(state.hasMicPermission, state.hasCameraPermission)
            rtc.setMicEnabled(state.micOn)
            rtc.setCameraEnabled(state.cameraOn)
            val speaker = audio.defaultSpeakerOn()
            audio.start(speaker)
            _uiState.update { it.copy(speakerOn = speaker) }
            if (state.hasMicPermission || state.hasCameraPermission) CallService.start(context, code)

            val client = SignalingClient(okHttpClient, json, viewModelScope, BuildConfig.BASE_URL, code, deviceId, name)
            signaling = client
            sessionJob?.cancel()
            sessionJob = launch {
                // UNDISPATCHED: subscribed before connect(), so the first frames are not missed.
                launch(start = CoroutineStart.UNDISPATCHED) { client.frames.collect { onFrame(it) } }
                launch(start = CoroutineStart.UNDISPATCHED) { client.state.collect { onSignalingState(it) } }
                launch(start = CoroutineStart.UNDISPATCHED) {
                    rtc.localCandidates.collect { c ->
                        client.send(ClientFrame.Ice(c.sdp, c.sdpMid, c.sdpMLineIndex))
                    }
                }
                launch(start = CoroutineStart.UNDISPATCHED) { rtc.iceState.collect { onIceState(it) } }
                launch {
                    NetworkUtils(context).observeNetworkState().collect { online -> if (online) client.reconnectNow() }
                }
            }
            client.connect()
        }
    }

    /** From ENDED (connection lost): same room, fresh seat. */
    fun retry() = join()

    // -- in call -----------------------------------------------------------------------------

    fun leave() {
        val phase = _uiState.value.phase
        if (phase == CallPhase.LOBBY || phase == CallPhase.ENDED) return
        signaling?.leave()
        end(EndReason.LEFT)
    }

    fun toggleMic() {
        val state = _uiState.value
        if (!state.hasMicPermission) return
        val on = !state.micOn
        rtc.setMicEnabled(on)
        _uiState.update { it.copy(micOn = on) }
        sendMediaState()
    }

    fun toggleCamera() {
        val state = _uiState.value
        if (!state.hasCameraPermission) return
        val on = !state.cameraOn
        rtc.setCameraEnabled(on)
        _uiState.update { it.copy(cameraOn = on) }
        sendMediaState()
    }

    fun switchCamera() = rtc.switchCamera()

    fun toggleSpeaker() {
        val on = !_uiState.value.speakerOn
        audio.setSpeaker(on)
        _uiState.update { it.copy(speakerOn = on) }
    }

    fun setChatOpen(open: Boolean) {
        _uiState.update { it.copy(chatOpen = open, unread = if (open) 0 else it.unread) }
    }

    // -- chat --------------------------------------------------------------------------------

    fun sendChat(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val id = UUID.randomUUID().toString()
        myMessageIds += id
        upsertMessage(
            ChatItem(id, mine = true, _uiState.value.displayName, clean, System.currentTimeMillis(), ChatStatus.SENDING)
        )
        // Not sent while reconnecting: resendPending() sends it once the room seats us again.
        signaling?.send(ClientFrame.Chat(id, clean))
        failIfUnconfirmed(id)
        onChatInputChanged("")
    }

    fun retryMessage(id: String) {
        val item = _uiState.value.messages.firstOrNull { it.id == id && it.status == ChatStatus.FAILED } ?: return
        upsertMessage(item.copy(status = ChatStatus.SENDING))
        signaling?.send(ClientFrame.Chat(id, item.text))
        failIfUnconfirmed(id)
    }

    /** "typing…" on the other phone: sent at most every 3 s while the text changes. */
    fun onChatInputChanged(text: String) {
        val now = SystemClock.elapsedRealtime()
        if (text.isNotBlank()) {
            if (!typingSent || now - lastTypingAt > TYPING_RESEND_MS) {
                typingSent = signaling?.send(ClientFrame.Typing(true)) == true
                lastTypingAt = now
            }
        } else if (typingSent) {
            signaling?.send(ClientFrame.Typing(false))
            typingSent = false
        }
    }

    private fun failIfUnconfirmed(id: String) {
        viewModelScope.launch {
            delay(CHAT_CONFIRM_TIMEOUT_MS)
            _uiState.update { state ->
                state.copy(messages = state.messages.map {
                    if (it.id == id && it.status == ChatStatus.SENDING) it.copy(status = ChatStatus.FAILED) else it
                })
            }
        }
    }

    private fun upsertMessage(item: ChatItem) {
        _uiState.update { state ->
            val index = state.messages.indexOfFirst { it.id == item.id }
            val messages = if (index >= 0) {
                state.messages.toMutableList().also { it[index] = item }
            } else {
                state.messages + item
            }
            state.copy(messages = messages)
        }
    }

    private fun resendPending() {
        _uiState.value.messages
            .filter { it.mine && it.status == ChatStatus.SENDING }
            .forEach { signaling?.send(ClientFrame.Chat(it.id, it.text)) }
    }

    // -- signaling ---------------------------------------------------------------------------

    private fun onSignalingState(state: SignalingClient.State) {
        when (state) {
            SignalingClient.State.Idle -> Unit
            is SignalingClient.State.Connecting -> _uiState.update { it.copy(reconnecting = state.attempt > 0) }
            SignalingClient.State.Open -> _uiState.update { it.copy(reconnecting = false) }
            is SignalingClient.State.Closed -> when (state.reason) {
                SignalingClient.CloseReason.LEFT -> Unit
                SignalingClient.CloseReason.ROOM_NOT_FOUND -> end(EndReason.ROOM_NOT_FOUND)
                SignalingClient.CloseReason.ROOM_FULL -> end(EndReason.ROOM_FULL)
                SignalingClient.CloseReason.REPLACED -> end(EndReason.REPLACED)
                SignalingClient.CloseReason.UNAUTHORIZED -> end(EndReason.UNAUTHORIZED)
                SignalingClient.CloseReason.GAVE_UP -> end(EndReason.CONNECTION_LOST)
            }
        }
    }

    private suspend fun onFrame(frame: ServerFrame) {
        when (frame) {
            is ServerFrame.Joined -> onJoined(frame)
            is ServerFrame.PeerJoined -> {
                // We were here first: answer the newcomer's offer on a fresh connection.
                offerer = false
                rtc.closePeer()
                rtc.createPeer(iceServers)
                _uiState.update { it.copy(peer = frame.peer.toUi()) }
                beginConnecting()
                _effects.tryEmit(CallEffect.PeerJoined(frame.peer.name))
            }
            is ServerFrame.PeerReconnecting -> updatePeer(frame.peerId) { it.copy(reconnecting = true) }
            is ServerFrame.PeerResumed -> {
                updatePeer(frame.peerId) { it.copy(reconnecting = false) }
                if (offerer && !rtc.isMediaConnected) negotiate(iceRestart = true)
            }
            is ServerFrame.PeerLeft -> {
                val peer = _uiState.value.peer ?: return
                if (peer.peerId != frame.peerId) return
                rtc.closePeer()
                toWaiting()
                _effects.tryEmit(CallEffect.PeerLeft(peer.name, timedOut = frame.reason == "timeout"))
            }
            is ServerFrame.Offer -> answer(frame.sdp)
            is ServerFrame.Answer -> negotiation.withLock {
                runCatching { rtc.acceptAnswer(frame.sdp) }.onFailure { Log.w(TAG, "Answer rejected", it) }
            }
            is ServerFrame.Ice -> rtc.addRemoteCandidate(IceCandidate(frame.sdpMid, frame.sdpMLineIndex ?: 0, frame.candidate))
            is ServerFrame.Renegotiate -> if (offerer) negotiate(iceRestart = true)
            is ServerFrame.MediaState -> updatePeer(frame.fromPeer) { it.copy(micOn = frame.audio, cameraOn = frame.video) }
            is ServerFrame.Typing -> {
                _uiState.update { it.copy(peerTyping = frame.typing) }
                typingClear?.cancel()
                if (frame.typing) {
                    typingClear = viewModelScope.launch {
                        delay(TYPING_EXPIRE_MS)
                        _uiState.update { it.copy(peerTyping = false) }
                    }
                }
            }
            is ServerFrame.Chat -> onChat(frame, live = true)
            is ServerFrame.Error -> {
                Log.w(TAG, "Server error ${frame.error}: ${frame.message}")
                frame.ref?.let { ref ->
                    _uiState.value.messages.firstOrNull { it.id == ref }
                        ?.let { upsertMessage(it.copy(status = ChatStatus.FAILED)) }
                }
            }
            is ServerFrame.Pong -> Unit
        }
    }

    private suspend fun onJoined(frame: ServerFrame.Joined) {
        iceServers = frame.iceServers
        frame.history.forEach { onChat(it, live = false) }
        resendPending()
        sendMediaState()
        val other = frame.peers.firstOrNull()
        if (frame.resumed && rtc.hasPeer) {
            // Our socket came back; the WebRTC connection may have survived the drop.
            if (other == null) {
                rtc.closePeer()
                toWaiting()
                return
            }
            _uiState.update { it.copy(peer = other.toUi()) }
            if (other.connected && !rtc.isMediaConnected) {
                if (offerer) negotiate(iceRestart = true) else signaling?.send(ClientFrame.Renegotiate)
            }
            return
        }
        // A fresh seat: first join, or our old seat expired during a long drop.
        rtc.closePeer()
        if (other == null) {
            offerer = false
            toWaiting()
        } else {
            offerer = true
            _uiState.update { it.copy(peer = other.toUi()) }
            beginConnecting()
            negotiate(iceRestart = false)
        }
    }

    private fun onChat(frame: ServerFrame.Chat, live: Boolean) {
        val mine = frame.id in myMessageIds || frame.fromPeer == signaling?.peerId
        upsertMessage(ChatItem(frame.id, mine, frame.name, frame.text, parseTime(frame.sentAt), ChatStatus.SENT))
        if (mine || !live) return
        typingClear?.cancel()
        val chatOpen = _uiState.value.chatOpen
        _uiState.update { it.copy(peerTyping = false, unread = if (chatOpen) 0 else it.unread + 1) }
        if (!chatOpen) _effects.tryEmit(CallEffect.IncomingMessage(frame.name, frame.text))
    }

    // -- WebRTC ------------------------------------------------------------------------------

    private suspend fun negotiate(iceRestart: Boolean) {
        negotiation.withLock {
            val client = signaling ?: return
            try {
                if (!rtc.hasPeer) rtc.createPeer(iceServers)
                client.send(ClientFrame.Offer(rtc.createOffer(iceRestart)))
            } catch (e: RtcException) {
                Log.w(TAG, "Offer failed", e)
            }
        }
    }

    private suspend fun answer(sdp: String) {
        negotiation.withLock {
            val client = signaling ?: return
            try {
                if (!rtc.hasPeer) rtc.createPeer(iceServers)
                client.send(ClientFrame.Answer(rtc.acceptOffer(sdp)))
            } catch (e: RtcException) {
                // An offer from a brand-new connection does not fit the old one: start over once.
                Log.w(TAG, "Offer did not apply, retrying on a fresh connection", e)
                runCatching {
                    rtc.createPeer(iceServers)
                    client.send(ClientFrame.Answer(rtc.acceptOffer(sdp)))
                }.onFailure { Log.w(TAG, "Answer failed", it) }
            }
        }
    }

    private fun onIceState(state: IceConnectionState) {
        when (state) {
            IceConnectionState.CONNECTED, IceConnectionState.COMPLETED -> {
                iceWatchdog?.cancel()
                troubleJob?.cancel()
                _uiState.update {
                    it.copy(
                        phase = if (it.peer != null) CallPhase.IN_CALL else it.phase,
                        mediaReconnecting = false,
                        connectTrouble = false,
                        callStartedAt = it.callStartedAt ?: SystemClock.elapsedRealtime(),
                    )
                }
            }
            // Often recovers by itself within a second or two (a brief radio hiccup).
            IceConnectionState.DISCONNECTED -> {
                _uiState.update { it.copy(mediaReconnecting = it.phase == CallPhase.IN_CALL) }
                iceWatchdog?.cancel()
                iceWatchdog = viewModelScope.launch {
                    delay(ICE_DISCONNECT_GRACE_MS)
                    if (rtc.iceState.value == IceConnectionState.DISCONNECTED) recoverMedia()
                }
            }
            IceConnectionState.FAILED -> recoverMedia()
            else -> Unit
        }
    }

    private fun recoverMedia() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastRestartAt < RESTART_MIN_INTERVAL_MS || _uiState.value.peer == null) return
        lastRestartAt = now
        _uiState.update { it.copy(mediaReconnecting = it.phase == CallPhase.IN_CALL) }
        if (offerer) {
            viewModelScope.launch { negotiate(iceRestart = true) }
        } else {
            signaling?.send(ClientFrame.Renegotiate)
        }
    }

    private fun beginConnecting() {
        _uiState.update {
            it.copy(phase = if (it.phase == CallPhase.IN_CALL) it.phase else CallPhase.CONNECTING, connectTrouble = false)
        }
        troubleJob?.cancel()
        troubleJob = viewModelScope.launch {
            delay(CONNECT_TROUBLE_AFTER_MS)
            if (!rtc.isMediaConnected) _uiState.update { it.copy(connectTrouble = true) }
        }
    }

    private fun toWaiting() {
        troubleJob?.cancel()
        iceWatchdog?.cancel()
        typingClear?.cancel()
        _uiState.update {
            it.copy(
                phase = CallPhase.WAITING,
                peer = null,
                callStartedAt = null,
                mediaReconnecting = false,
                connectTrouble = false,
                peerTyping = false,
            )
        }
    }

    private fun sendMediaState() {
        val state = _uiState.value
        signaling?.send(
            ClientFrame.MediaState(
                audio = state.micOn && state.hasMicPermission,
                video = state.cameraOn && state.hasCameraPermission,
            )
        )
    }

    private fun updatePeer(peerId: String, change: (PeerUi) -> PeerUi) {
        _uiState.update { state ->
            val peer = state.peer
            if (peer == null || peer.peerId != peerId) state else state.copy(peer = change(peer))
        }
    }

    /** Must not suspend: it may run inside the session job it cancels. */
    private fun end(reason: EndReason) {
        sessionJob?.cancel()
        sessionJob = null
        signaling = null
        iceWatchdog?.cancel()
        troubleJob?.cancel()
        typingClear?.cancel()
        rtc.closePeer()
        // stopCapture() blocks while the camera closes: keep it off the main thread.
        stopMediaJob = viewModelScope.launch(Dispatchers.Default) { rtc.stopLocalMedia() }
        audio.stop()
        CallService.stop(context)
        _uiState.update {
            it.copy(
                phase = CallPhase.ENDED,
                endReason = reason,
                endedCallMillis = it.callStartedAt?.let { start -> SystemClock.elapsedRealtime() - start },
                peer = null,
                callStartedAt = null,
                reconnecting = false,
                mediaReconnecting = false,
                connectTrouble = false,
                chatOpen = false,
            )
        }
    }

    override fun onCleared() {
        if (_uiState.value.phase != CallPhase.ENDED) {
            signaling?.leave()
            end(EndReason.LEFT)
        }
        rtc.release()
        eglBase.release()
    }

    private fun PeerInfo.toUi() = PeerUi(peerId, name, micOn = audio, cameraOn = video, reconnecting = !connected)

    private fun parseTime(iso: String): Long = runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).parse(iso)?.time
    }.getOrNull() ?: System.currentTimeMillis()

    private companion object {
        const val TAG = "CallVM"
        const val MAX_NAME_CHARS = 40
        const val TYPING_RESEND_MS = 3_000L
        const val TYPING_EXPIRE_MS = 5_000L
        const val CHAT_CONFIRM_TIMEOUT_MS = 15_000L
        const val ICE_DISCONNECT_GRACE_MS = 2_500L
        const val RESTART_MIN_INTERVAL_MS = 4_000L
        const val CONNECT_TROUBLE_AFTER_MS = 20_000L
    }
}
