package com.alantech.boardgame.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alantech.boardgame.ui.model.PackDetailUIModel

@Entity(tableName = "local_pack")
data class LocalPackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverImageUri: String?,
    val tag: String,
    val heatLevel: Int,
    val estimateTimePlay: Int,
    val suggestNumberPlayers: Int,
    val howToPlay: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun LocalPackEntity.toPackDetailUIModel(): PackDetailUIModel = PackDetailUIModel(
    id = id,
    thumbnail = coverImageUri.orEmpty(),
    titleCard = title,
    creator = tag.ifBlank { "Custom" },
    description = description,
    coverImageUrl = coverImageUri,
    thumb = coverImageUri,
    estimateTimePlay = estimateTimePlay,
    suggestNumberPlayers = suggestNumberPlayers,
    tag = tag,
    heatLevel = heatLevel,
    howToPlay = howToPlay
)
