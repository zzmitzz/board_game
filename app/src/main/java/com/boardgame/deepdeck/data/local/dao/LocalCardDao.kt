package com.boardgame.deepdeck.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boardgame.deepdeck.data.local.entity.LocalCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalCardDao {
    @Query("SELECT * FROM local_card WHERE packId = :packId ORDER BY createdAt ASC")
    fun getCardsForPack(packId: String): Flow<List<LocalCardEntity>>

    /** Card count per custom pack (Library › My packs). */
    @Query("SELECT packId, COUNT(*) AS count FROM local_card GROUP BY packId")
    fun observeCardCounts(): Flow<List<PackCardCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: LocalCardEntity)

    @Delete
    suspend fun delete(card: LocalCardEntity)
}

data class PackCardCount(val packId: String, val count: Int)
