package com.boardgame.deepdeck.features.home.model

import androidx.annotation.DrawableRes
import com.boardgame.deepdeck.data.model.VibeCategory

data class VibeChip(
    val id: String,
    val name: String,
    @DrawableRes val icon: Int?
)

fun VibeCategory.toVibeChip(): VibeChip{
    return VibeChip(
        id = id!!,
        name = this.categoryEn ?: "Null",
        icon = null
    )
}