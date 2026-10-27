package com.boardgame.deepdeck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** What a captured moment holds; stored as its [name] in `moment.type`. */
enum class MomentType(val extension: String, val mimeType: String) {
    PHOTO("jpg", "image/jpeg"),
    VIDEO("mp4", "video/mp4"),
    AUDIO("m4a", "audio/mp4");

    companion object {
        fun from(raw: String?): MomentType = entries.firstOrNull { it.name == raw } ?: PHOTO
    }
}

/**
 * A photo / video / voice clip captured during a game ("Capture moments" house rule).
 * The media lives in app-private storage; [filePath] is relative to `filesDir`
 * (e.g. `moments/<id>.jpg`). Nothing here ever leaves the device.
 */
@Entity(tableName = "moment")
data class MomentEntity(
    @PrimaryKey val id: String,
    /** The game session it was captured in (see `GameResult.sessionId`). */
    val sessionId: String,
    /** [MomentType] name. */
    val type: String,
    val filePath: String,
    /** Voice clips only. */
    val durationMs: Long? = null,
    val cardText: String? = null,
    val playerName: String? = null,
    val packTitle: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val momentType: MomentType get() = MomentType.from(type)
}
