package com.alantech.boardgame.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtils {

    /**
     * Light, subtle click — great for standard button taps or small interactions.
     */
    fun short(context: Context) {
        vibrate(context, VibrationEffect.EFFECT_CLICK, longArrayOf(0, 20), intArrayOf(0, 150))
    }

    /**
     * Longer, single pulse — great for held buttons, errors, or major UI triggers.
     */
    fun long(context: Context) {
        vibrate(context, VibrationEffect.EFFECT_HEAVY_CLICK, longArrayOf(0, 100), intArrayOf(0, 255))
    }

    /**
     * Two quick, crisp pulses — signals progression/transitioning into the next round.
     */
    fun nextRound(context: Context) {
        val pattern = longArrayOf(0, 40, 60, 60) // wait, vibrate, wait, vibrate
        val amplitudes = intArrayOf(0, 180, 0, 255) // building intensity
        vibratePattern(context, pattern, amplitudes)
    }

    /**
     * Three-pulse climax signature — heavy finality to signal game over.
     */
    fun endGame(context: Context) {
        val pattern = longArrayOf(0, 80, 50, 80, 50, 200)
        val amplitudes = intArrayOf(0, 200, 0, 200, 0, 255)
        vibratePattern(context, pattern, amplitudes)
    }

    // --- Helper Methods ---

    private fun vibrate(context: Context, predefinedEffect: Int, fallbackPattern: LongArray, fallbackAmplitudes: IntArray) {
        val vibrator = getVibrator(context) ?: return

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(predefinedEffect))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(fallbackPattern, fallbackAmplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(fallbackPattern[1])
        }
    }

    private fun vibratePattern(context: Context, pattern: LongArray, amplitudes: IntArray) {
        val vibrator = getVibrator(context) ?: return

        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}