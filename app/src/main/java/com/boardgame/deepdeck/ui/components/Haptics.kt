package com.boardgame.deepdeck.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import com.boardgame.deepdeck.utils.HapticUtils

/**
 * Haptics map (plan §2.4):
 * press = TextHandleMove/light · swipe commit = Confirm · forfeit = double tick ·
 * round end = medium · win = long pattern.
 * All calls are no-ops when the user turned haptics off in the You tab.
 */
@Stable
class DeepTalkHaptics(
    private val view: View?,
    private val isEnabled: () -> Boolean,
) {
    private fun perform(constant: Int) {
        if (!isEnabled()) return
        view?.performHapticFeedback(constant)
    }

    /** Light tick for button / tile presses. */
    fun press() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) HapticFeedbackConstants.TEXT_HANDLE_MOVE
        else HapticFeedbackConstants.KEYBOARD_TAP
    )

    /** Swipe commit / positive confirmation. */
    fun confirm() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
        else HapticFeedbackConstants.VIRTUAL_KEY
    )

    /** Forfeit: two quick ticks. */
    fun doubleTick() {
        if (!isEnabled()) return
        val v = view ?: return
        v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        v.postDelayed({ v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK) }, 90)
    }

    /** Round end / reveal moments. */
    fun medium() = perform(HapticFeedbackConstants.CONTEXT_CLICK)

    /** Win: long signature pattern. */
    fun win() {
        if (!isEnabled()) return
        val v = view ?: return
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        HapticUtils.endGame(v.context)
    }
}

val LocalDeepTalkHaptics = staticCompositionLocalOf { DeepTalkHaptics(null) { false } }

@Composable
fun rememberDeepTalkHaptics(isEnabled: () -> Boolean): DeepTalkHaptics {
    val view = LocalView.current
    return remember(view) { DeepTalkHaptics(view, isEnabled) }
}
