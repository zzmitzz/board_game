package com.alantech.boardgame.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import com.alantech.boardgame.R

enum class ListSound(
    @RawRes val rawID: Int
) {
    PLAY(R.raw.play),
    START(R.raw.start),
    SUCCESS(R.raw.success),
    ACHIEVEMENT(R.raw.achievement)
}

object SoundUtils {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<ListSound, Int>()
    private var isLoaded = false

    fun init(context: Context) {
        if (soundPool != null) return // Avoid re-initialization

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME) // Or USAGE_ASSISTANCE_SONIFICATION for UI sounds
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // Max simultaneous sounds playing at once
            .setAudioAttributes(audioAttributes)
            .build()
        ListSound.entries.forEach { loadSound(context, it) }
    }

    fun loadSound(context: Context, soundKey: ListSound) {
        soundPool?.let { pool ->
            val soundId = pool.load(context, soundKey.rawID, 1)
            soundMap[soundKey] = soundId
        }
    }

    fun play(soundKey: ListSound, volume: Float = 1.0f) {
        val soundId = soundMap[soundKey] ?: return

        // play(soundID, leftVolume, rightVolume, priority, loop, rate)
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
    }
}