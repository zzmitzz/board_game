package com.alantech.boardgame.utils

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import kotlin.random.Random
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider


interface DialogListener{
    fun onCancel()
    fun onDismiss()
}

abstract class DialogPlayerListener : DialogListener{
    abstract fun onConfirm(numberPlayer: Int)
}

abstract class SettingDialogListener: DialogListener{
    abstract fun onConfirm(hapticEnabled: Boolean, soundEnabled: Boolean)
}

@Composable
fun BlurBackgroundDialog(
    listener: DialogListener?,
    content: @Composable () -> Unit
){
    Dialog(
        onDismissRequest = {
            listener?.onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                dialogWindow?.apply {
                    attributes.blurBehindRadius = 50
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                }
            }
        }
        content.invoke()
    }
}



fun Modifier.addOutlineBorder() = this.border(
    width = 1.dp,
    color = Color.White.copy(alpha = 0.2f)
)

fun Modifier.addBgGradient(
    startColor: Color = Color.Black.copy(alpha = 0.5f),
    endColor: Color = Color.Transparent,
) = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(startColor,endColor.copy(alpha = 0.7f), endColor),
        startY = 0f
    )
)


// Extension property for easy use
val Color.Companion.random: Color
    get() = Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1f // 1f means fully opaque
    )


fun getListGradientColorPacks() = listOf(
    listOf(Color(0xFFA305F7), Color(0xFFD219D7), Color(0xFFFA2DB6)),
    listOf(Color(0xFF07C8F9), Color(0xFF0A85ED), Color(0xFF0D41E1)),
    listOf(Color(0xFFFF4E50), Color(0xFFFC913A), Color(0xFFF9D423)),
    listOf(Color(0xFF11998E), Color(0xFF38EF7D), Color(0xFF11D4A0)),
    listOf(Color(0xFFFFD000), Color(0xFFFF8C00), Color(0xFFFF4500)),
)


const val clickInterval: Long = 800L