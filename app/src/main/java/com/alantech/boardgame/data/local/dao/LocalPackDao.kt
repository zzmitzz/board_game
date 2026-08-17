package com.alantech.boardgame.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alantech.boardgame.data.local.entity.LocalPackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalPackDao {
    @Query("SELECT * FROM local_pack ORDER BY createdAt DESC")
    fun getAllPacks(): Flow<List<LocalPackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: LocalPackEntity)

    @Delete
    suspend fun delete(pack: LocalPackEntity)
}
