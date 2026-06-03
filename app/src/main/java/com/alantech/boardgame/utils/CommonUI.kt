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


@Composable
fun ButtonText(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

}


interface DialogListener{
    fun onConfirm(numberPlayer: Int)
    fun onCancel()
    fun onDismiss()
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