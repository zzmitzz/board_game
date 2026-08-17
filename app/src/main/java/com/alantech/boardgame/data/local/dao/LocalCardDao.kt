package com.alantech.boardgame.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alantech.boardgame.data.local.entity.LocalCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalCardDao {
    @Query("SELECT * FROM local_card WHERE packId = :packId ORDER BY createdAt ASC")
    fun getCardsForPack(packId: String): Flow<List<LocalCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: LocalCardEntity)

    @Delete
    suspend fun delete(card: LocalCardEntity)
}
