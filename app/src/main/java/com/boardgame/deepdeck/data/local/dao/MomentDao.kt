package com.boardgame.deepdeck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boardgame.deepdeck.data.local.entity.MomentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MomentDao {
    @Query("SELECT * FROM moment ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MomentEntity>>

    @Query("SELECT * FROM moment WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    fun observeBySession(sessionId: String): Flow<List<MomentEntity>>

    @Query("SELECT * FROM moment WHERE id = :id")
    fun observeById(id: String): Flow<MomentEntity?>

    @Query("SELECT COUNT(*) FROM moment")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM moment WHERE id = :id")
    suspend fun getById(id: String): MomentEntity?

    @Query("SELECT filePath FROM moment")
    suspend fun allFilePaths(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moment: MomentEntity)

    @Query("UPDATE moment SET durationMs = :durationMs WHERE id = :id")
    suspend fun updateDuration(id: String, durationMs: Long)

    @Query("DELETE FROM moment WHERE id = :id")
    suspend fun deleteById(id: String)
}
