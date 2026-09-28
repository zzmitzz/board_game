package com.boardgame.deepdeck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCardDao {
    @Query("SELECT * FROM saved_card ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedCardEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_card WHERE id = :id)")
    fun observeIsSaved(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: SavedCardEntity)

    @Query("DELETE FROM saved_card WHERE id = :id")
    suspend fun deleteById(id: String)
}
