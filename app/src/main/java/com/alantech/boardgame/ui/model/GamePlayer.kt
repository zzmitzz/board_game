package com.alantech.boardgame.ui.model

import android.graphics.Bitmap
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color

data class GamePlayer(
    val id: Int,
    val color: Color,
    val name: String,
)