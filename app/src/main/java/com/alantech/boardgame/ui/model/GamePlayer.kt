package com.alantech.boardgame.ui.model

import android.graphics.Bitmap

data class GamePlayer(
    val id: Int,
    val image: Bitmap,
    val name: String,
)