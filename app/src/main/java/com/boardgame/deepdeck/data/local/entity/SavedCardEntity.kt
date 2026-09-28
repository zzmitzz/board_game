package com.boardgame.deepdeck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A question the user saved with ♥ (Tonight's Card, gameplay, …). */
@Entity(tableName = "saved_card")
data class SavedCardEntity(
    @PrimaryKey val id: String,
    val text: String,
    val back: String? = null,
    val level: String? = null,
    val packId: String? = null,
    val packTitle: String? = null,
    val savedAt: Long = System.currentTimeMillis(),
)
