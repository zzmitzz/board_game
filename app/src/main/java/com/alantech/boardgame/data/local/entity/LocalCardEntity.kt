package com.alantech.boardgame.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.CardDetailMedia

@Entity(
    tableName = "local_card",
    foreignKeys = [
        ForeignKey(
            entity = LocalPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("packId")]
)
data class LocalCardEntity(
    @PrimaryKey val id: String,
    val packId: String,
    val category: String,
    val description: String,
    val hint: String,
    val mediaImageUri: String?,
    val createdAt: Long = System.currentTimeMillis()
)

fun LocalCardEntity.toCardDetail(): CardDetail = CardDetail(
    id = id,
    category = category,
    description = description,
    hint = hint,
    media = CardDetailMedia(image = mediaImageUri, video = null)
)
