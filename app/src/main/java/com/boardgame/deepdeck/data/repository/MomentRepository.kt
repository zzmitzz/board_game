package com.boardgame.deepdeck.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.boardgame.deepdeck.data.local.dao.MomentDao
import com.boardgame.deepdeck.data.local.entity.MomentEntity
import com.boardgame.deepdeck.data.local.entity.MomentType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Captured moments: media files in `filesDir/moments/` (app-private, excluded from backups)
 * + their metadata in Room `moment`. Paths are stored relative to `filesDir`.
 */
@Singleton
class MomentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: MomentDao,
) {
    fun observeAll(): Flow<List<MomentEntity>> = dao.observeAll()

    fun observeSession(sessionId: String): Flow<List<MomentEntity>> = dao.observeBySession(sessionId)

    fun observe(id: String): Flow<MomentEntity?> = dao.observeById(id)

    fun observeCount(): Flow<Int> = dao.observeCount()

    /** Absolute file for a stored (relative) path. */
    fun fileOf(relativePath: String): File = File(context.filesDir, relativePath)

    fun fileOf(moment: MomentEntity): File = fileOf(moment.filePath)

    /** content:// for the camera app (write) or the share sheet (read). */
    fun uriOf(relativePath: String): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fileOf(relativePath))

    /** Reserves `moments/<id>.<ext>` (directory created, file not); returns the id + relative path. */
    fun newTarget(type: MomentType): Pair<String, String> {
        File(context.filesDir, DIR).mkdirs()
        val id = UUID.randomUUID().toString()
        return id to "$DIR/$id.${type.extension}"
    }

    /** Inserts the row when the file exists and isn't empty; otherwise deletes it. Returns success. */
    suspend fun save(moment: MomentEntity): Boolean = withContext(Dispatchers.IO) {
        val file = fileOf(moment)
        if (!file.isFile || file.length() == 0L) {
            file.delete()
            return@withContext false
        }
        runCatching { dao.insert(moment) }
            .onFailure {
                Log.w(TAG, "insert failed: ${it.message}")
                file.delete()
            }
            .isSuccess
    }

    /** Fixes a clip length measured by the player (keeps tiles and viewer in agreement). */
    suspend fun updateDuration(id: String, durationMs: Long) = dao.updateDuration(id, durationMs)

    /** Drops a capture that was cancelled or failed. */
    suspend fun discard(relativePath: String) = withContext(Dispatchers.IO) {
        fileOf(relativePath).delete()
    }

    /** Deletes the file (if still there) and the row. */
    suspend fun delete(moment: MomentEntity) = withContext(Dispatchers.IO) {
        fileOf(moment).delete()
        dao.deleteById(moment.id)
    }

    /** Removes files no row points to (e.g. a capture interrupted by process death). */
    suspend fun deleteOrphanFiles() = withContext(Dispatchers.IO) {
        runCatching {
            val known = dao.allFilePaths().map { fileOf(it).name }.toSet()
            File(context.filesDir, DIR).listFiles()?.forEach { if (it.name !in known) it.delete() }
        }.onFailure { Log.w(TAG, "orphan cleanup failed: ${it.message}") }
    }

    private companion object {
        const val TAG = "MomentRepository"
        const val DIR = "moments"
    }
}
