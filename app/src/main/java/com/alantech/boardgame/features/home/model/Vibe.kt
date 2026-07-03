package com.alantech.boardgame.features.home.model

import androidx.annotation.DrawableRes
import com.alantech.boardgame.data.model.VibeCategory

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