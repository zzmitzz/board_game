package com.boardgame.deepdeck.features.moments

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import android.util.Log
import java.io.File

/** Voice clips auto-stop (and save) at 2 minutes. */
const val MAX_VOICE_MS = 120_000L

/**
 * Thin [MediaRecorder] wrapper for voice moments (MPEG-4 / AAC → `.m4a`).
 * Owned by the game ViewModel so a recording survives rotation; every path ends in [release].
 * Calls are blocking (prepare/stop take ~100–400 ms): run them off the main thread. Synchronized
 * because the ViewModel may release from the main thread while an IO call is in flight.
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var output: File? = null
    private var startedAt = 0L

    val isRecording: Boolean @Synchronized get() = recorder != null

    /** Starts recording into [file]; false if the mic/encoder could not be opened. */
    @Synchronized
    fun start(file: File): Boolean {
        release()
        @Suppress("DEPRECATION")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(BIT_RATE)
            r.setAudioSamplingRate(SAMPLE_RATE)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            output = file
            startedAt = SystemClock.elapsedRealtime()
            true
        } catch (e: Exception) {
            Log.w(TAG, "start failed: ${e.message}")
            r.release()
            false
        }
    }

    /** Stops and finalizes the file; returns the clip length, or null when nothing valid was recorded. */
    @Synchronized
    fun stop(): Long? {
        val r = recorder ?: return null
        recorder = null
        val elapsed = SystemClock.elapsedRealtime() - startedAt
        val stopped = try {
            r.stop()
            true
        } catch (e: RuntimeException) {
            // Thrown when stop() follows start() too closely (no audio data).
            Log.w(TAG, "stop failed: ${e.message}")
            false
        } finally {
            r.release()
        }
        if (!stopped) return null
        // The encoded length is the truth (the mic can stall); wall-clock time is the fallback.
        return output?.let(::fileDuration) ?: elapsed
    }

    private fun fileDuration(file: File): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        } catch (_: RuntimeException) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    /** Stops without keeping anything (the caller deletes the file). */
    @Synchronized
    fun release() {
        val r = recorder ?: return
        recorder = null
        runCatching { r.stop() }
        r.release()
    }

    private companion object {
        const val TAG = "VoiceRecorder"
        const val BIT_RATE = 96_000
        const val SAMPLE_RATE = 44_100
    }
}
