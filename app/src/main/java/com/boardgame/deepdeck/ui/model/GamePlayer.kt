package com.boardgame.deepdeck.ui.model

import android.graphics.Bitmap
import androidx.annotation.RawRes
import androidx.compose.ui.graphics.Color
import com.boardgame.deepdeck.utils.serializable.CustomColorSerialization
import kotlinx.serialization.Serializable


@Serializable
data class GamePlayer(
    val id: Int,
    @Serializable(with = CustomColorSerialization::class)
    val color: Color,
    val name: String,
)