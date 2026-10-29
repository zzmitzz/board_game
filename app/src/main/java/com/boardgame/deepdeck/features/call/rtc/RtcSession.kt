package com.boardgame.deepdeck.features.call.rtc

import android.content.Context
import android.util.Log
import com.boardgame.deepdeck.features.call.signaling.IceServerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RtcException(message: String?) : Exception(message)

/**
 * The WebRTC side of a call: camera + mic capture and the one [PeerConnection] to the other phone.
 *
 * Signaling is not done here. The ViewModel moves SDP and ICE between this class and the
 * SignalingClient: [createOffer]/[acceptOffer]/[acceptAnswer] return or take SDP text, and
 * [localCandidates] emits the candidates to send.
 */
class RtcSession(
    private val context: Context,
    val eglBase: EglBase,
    private val scope: CoroutineScope,
) {
    private val factory: PeerConnectionFactory

    init {
        if (initialized.compareAndSet(false, true)) {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                    .createInitializationOptions()
            )
        }
        // Hardware echo cancellation + noise suppression: the phone's speaker sits next to its mic.
        val audioDevice = JavaAudioDeviceModule.builder(context.applicationContext)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()
        factory = PeerConnectionFactory.builder()
            .setAudioDeviceModule(audioDevice)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
        // The factory keeps its own reference.
        audioDevice.release()
    }

    // -- local media -------------------------------------------------------------------------

    private var capturer: CameraVideoCapturer? = null
    private var textureHelper: SurfaceTextureHelper? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null
    private var capturing = false

    private val _localVideo = MutableStateFlow<VideoTrack?>(null)
    val localVideo: StateFlow<VideoTrack?> = _localVideo.asStateFlow()

    private val _frontCamera = MutableStateFlow(true)
    val frontCamera: StateFlow<Boolean> = _frontCamera.asStateFlow()

    /** Creates the tracks the granted permissions allow; safe to call again after a new grant. */
    @Synchronized
    fun startLocalMedia(withAudio: Boolean, withVideo: Boolean) {
        if (withAudio && audioTrack == null) {
            audioSource = factory.createAudioSource(MediaConstraints())
            audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        }
        if (withVideo && _localVideo.value == null) {
            val camera = createCapturer() ?: return
            val helper = SurfaceTextureHelper.create("CameraCapture", eglBase.eglBaseContext)
            val source = factory.createVideoSource(camera.isScreencast)
            camera.initialize(helper, context.applicationContext, source.capturerObserver)
            capturer = camera
            textureHelper = helper
            videoSource = source
            _localVideo.value = factory.createVideoTrack(VIDEO_TRACK_ID, source)
            setCapturing(true)
        }
    }

    private fun createCapturer(): CameraVideoCapturer? {
        val enumerator = if (Camera2Enumerator.isSupported(context)) Camera2Enumerator(context) else Camera1Enumerator(true)
        val names = enumerator.deviceNames
        val name = names.firstOrNull { enumerator.isFrontFacing(it) } ?: names.firstOrNull() ?: return null
        _frontCamera.value = enumerator.isFrontFacing(name)
        return enumerator.createCapturer(name, null)
    }

    val hasAudio: Boolean get() = audioTrack != null
    val hasVideo: Boolean get() = _localVideo.value != null

    fun setMicEnabled(enabled: Boolean) {
        audioTrack?.setEnabled(enabled)
    }

    /** Off = the camera really stops (LED off, no battery), not just black frames. */
    fun setCameraEnabled(enabled: Boolean) {
        _localVideo.value?.setEnabled(enabled)
        setCapturing(enabled)
    }

    private fun setCapturing(on: Boolean) {
        val camera = capturer ?: return
        if (on == capturing) return
        capturing = on
        // start/stopCapture block while the camera opens or closes.
        scope.launch(Dispatchers.Default) {
            runCatching {
                if (on) camera.startCapture(CAPTURE_WIDTH, CAPTURE_HEIGHT, CAPTURE_FPS) else camera.stopCapture()
            }.onFailure { Log.w(TAG, "Camera ${if (on) "start" else "stop"} failed", it) }
        }
    }

    fun switchCamera() {
        capturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                _frontCamera.value = isFrontCamera
            }

            override fun onCameraSwitchError(errorDescription: String?) {
                Log.w(TAG, "Camera switch failed: $errorDescription")
            }
        })
    }

    @Synchronized
    fun stopLocalMedia() {
        val camera = capturer
        capturer = null
        capturing = false
        _localVideo.value?.let { track -> runCatching { track.dispose() } }
        _localVideo.value = null
        audioTrack?.let { track -> runCatching { track.dispose() } }
        audioTrack = null
        camera?.let {
            runCatching { it.stopCapture() }
            it.dispose()
        }
        videoSource?.dispose()
        videoSource = null
        audioSource?.dispose()
        audioSource = null
        textureHelper?.dispose()
        textureHelper = null
    }

    // -- peer connection ---------------------------------------------------------------------

    private var peer: PeerConnection? = null
    private val pendingCandidates = mutableListOf<IceCandidate>()

    private val _remoteVideo = MutableStateFlow<VideoTrack?>(null)
    val remoteVideo: StateFlow<VideoTrack?> = _remoteVideo.asStateFlow()

    private val _iceState = MutableStateFlow(PeerConnection.IceConnectionState.NEW)
    val iceState: StateFlow<PeerConnection.IceConnectionState> = _iceState.asStateFlow()

    private val _localCandidates = MutableSharedFlow<IceCandidate>(extraBufferCapacity = 128)
    val localCandidates: SharedFlow<IceCandidate> = _localCandidates.asSharedFlow()

    val hasPeer: Boolean get() = peer != null

    val isMediaConnected: Boolean
        get() = _iceState.value == PeerConnection.IceConnectionState.CONNECTED ||
            _iceState.value == PeerConnection.IceConnectionState.COMPLETED

    /** A fresh connection to a (new) peer; the old one, if any, is closed first. */
    @Synchronized
    fun createPeer(iceServers: List<IceServerInfo>) {
        closePeer()
        val servers = iceServers.map { server ->
            PeerConnection.IceServer.builder(server.urls)
                .apply {
                    server.username?.let { setUsername(it) }
                    server.credential?.let { setPassword(it) }
                }
                .createIceServer()
        }
        val config = PeerConnection.RTCConfiguration(servers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            // Keep gathering after connecting, so a Wi-Fi → mobile switch finds new paths.
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        }
        val connection = factory.createPeerConnection(config, observer)
            ?: throw RtcException("Could not create a peer connection")
        // Without a local track (permission denied) still ask to receive the other side's media.
        audioTrack?.let { connection.addTrack(it, listOf(STREAM_ID)) }
            ?: connection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO, receiveOnly())
        _localVideo.value?.let { connection.addTrack(it, listOf(STREAM_ID)) }
            ?: connection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO, receiveOnly())
        peer = connection
        _iceState.value = PeerConnection.IceConnectionState.NEW
    }

    private fun receiveOnly() =
        RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)

    /** Offerer: the SDP to send. [iceRestart] = new ICE credentials, so both sides gather again. */
    suspend fun createOffer(iceRestart: Boolean): String {
        val connection = peer ?: throw RtcException("No peer connection")
        val constraints = MediaConstraints().apply {
            if (iceRestart) mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
        }
        val offer = connection.awaitCreate(offer = true, constraints)
        connection.awaitSet(local = true, offer)
        return offer.description
    }

    /** Answerer: applies the offer and returns the answer SDP to send back. */
    suspend fun acceptOffer(sdp: String): String {
        val connection = peer ?: throw RtcException("No peer connection")
        connection.awaitSet(local = false, SessionDescription(SessionDescription.Type.OFFER, sdp))
        drainCandidates(connection)
        val answer = connection.awaitCreate(offer = false, MediaConstraints())
        connection.awaitSet(local = true, answer)
        return answer.description
    }

    suspend fun acceptAnswer(sdp: String) {
        val connection = peer ?: return
        if (connection.signalingState() != PeerConnection.SignalingState.HAVE_LOCAL_OFFER) return
        connection.awaitSet(local = false, SessionDescription(SessionDescription.Type.ANSWER, sdp))
        drainCandidates(connection)
    }

    /** Candidates can arrive before the offer/answer they belong to: hold them until then. */
    @Synchronized
    fun addRemoteCandidate(candidate: IceCandidate) {
        val connection = peer ?: return
        if (connection.remoteDescription == null) pendingCandidates += candidate
        else connection.addIceCandidate(candidate)
    }

    @Synchronized
    private fun drainCandidates(connection: PeerConnection) {
        pendingCandidates.forEach { connection.addIceCandidate(it) }
        pendingCandidates.clear()
    }

    @Synchronized
    fun closePeer() {
        val connection = peer ?: return
        peer = null
        pendingCandidates.clear()
        _remoteVideo.value = null
        _iceState.value = PeerConnection.IceConnectionState.CLOSED
        connection.dispose()
    }

    fun release() {
        closePeer()
        stopLocalMedia()
        factory.dispose()
    }

    private val observer = object : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            _localCandidates.tryEmit(candidate)
        }

        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
            Log.d(TAG, "ICE $newState")
            _iceState.value = newState
        }

        override fun onTrack(transceiver: RtpTransceiver) {
            val track = transceiver.receiver.track()
            if (track is VideoTrack) _remoteVideo.value = track
        }

        override fun onSignalingChange(newState: PeerConnection.SignalingState) = Unit
        override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) = Unit
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
        override fun onAddStream(stream: MediaStream) = Unit
        override fun onRemoveStream(stream: MediaStream) = Unit
        override fun onDataChannel(channel: DataChannel) = Unit
        // Negotiation is explicit (the offerer offers when the server says so).
        override fun onRenegotiationNeeded() = Unit
    }

    private suspend fun PeerConnection.awaitCreate(offer: Boolean, constraints: MediaConstraints): SessionDescription =
        suspendCancellableCoroutine { cont ->
            val callback = object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    if (cont.isActive) cont.resume(sdp)
                }

                override fun onCreateFailure(error: String?) {
                    if (cont.isActive) cont.resumeWithException(RtcException(error))
                }

                override fun onSetSuccess() = Unit
                override fun onSetFailure(error: String?) = Unit
            }
            if (offer) createOffer(callback, constraints) else createAnswer(callback, constraints)
        }

    private suspend fun PeerConnection.awaitSet(local: Boolean, sdp: SessionDescription): Unit =
        suspendCancellableCoroutine { cont ->
            val callback = object : SdpObserver {
                override fun onSetSuccess() {
                    if (cont.isActive) cont.resume(Unit)
                }

                override fun onSetFailure(error: String?) {
                    if (cont.isActive) cont.resumeWithException(RtcException(error))
                }

                override fun onCreateSuccess(sdp: SessionDescription) = Unit
                override fun onCreateFailure(error: String?) = Unit
            }
            if (local) setLocalDescription(callback, sdp) else setRemoteDescription(callback, sdp)
        }

    private companion object {
        const val TAG = "RtcSession"
        const val STREAM_ID = "deepdeck"
        const val AUDIO_TRACK_ID = "mic"
        const val VIDEO_TRACK_ID = "camera"
        const val CAPTURE_WIDTH = 1280
        const val CAPTURE_HEIGHT = 720
        const val CAPTURE_FPS = 30
        val initialized = AtomicBoolean(false)
    }
}
