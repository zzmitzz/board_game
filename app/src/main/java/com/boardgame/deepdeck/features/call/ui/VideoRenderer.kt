package com.boardgame.deepdeck.features.call.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

/**
 * Draws a WebRTC [VideoTrack] with the GL-backed [SurfaceViewRenderer].
 *
 * The track pushes frames into the renderer (a "sink") from the moment [VideoTrack.addSink] runs
 * until [VideoTrack.removeSink]; both follow the composable's lifetime. [overlay] = drawn above
 * another video (the small self view), since two SurfaceViews otherwise fight over z-order.
 */
@Composable
fun VideoRenderer(
    track: VideoTrack?,
    eglContext: EglBase.Context,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    overlay: Boolean = false,
    fill: Boolean = true,
) {
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglContext, null)
                setEnableHardwareScaler(true)
                setZOrderMediaOverlay(overlay)
                renderer = this
            }
        },
        update = { view ->
            view.setMirror(mirror)
            view.setScalingType(
                if (fill) RendererCommon.ScalingType.SCALE_ASPECT_FILL
                else RendererCommon.ScalingType.SCALE_ASPECT_FIT
            )
        },
        onRelease = { view ->
            renderer = null
            view.release()
        }
    )

    DisposableEffect(track, renderer) {
        val view = renderer
        if (track != null && view != null) runCatching { track.addSink(view) }
        onDispose {
            // The track may already be disposed (the peer connection closed first).
            if (track != null && view != null) runCatching { track.removeSink(view) }
            view?.clearImage()
        }
    }
}
