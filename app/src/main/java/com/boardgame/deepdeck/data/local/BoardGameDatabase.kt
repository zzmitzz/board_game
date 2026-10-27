package com.boardgame.deepdeck.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boardgame.deepdeck.data.local.dao.GameResultDao
import com.boardgame.deepdeck.data.local.dao.LocalCardDao
import com.boardgame.deepdeck.data.local.dao.LocalPackDao
import com.boardgame.deepdeck.data.local.dao.MomentDao
import com.boardgame.deepdeck.data.local.dao.SavedCardDao
import com.boardgame.deepdeck.data.local.entity.GameResult
import com.boardgame.deepdeck.data.local.entity.LocalCardEntity
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import com.boardgame.deepdeck.data.local.entity.MomentEntity
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        GameResult::class, LocalPackEntity::class, LocalCardEntity::class, SavedCardEntity::class, MomentEntity::class,
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(GameResultTypeConverters::class)
abstract class BoardGameDatabase : RoomDatabase() {

    abstract fun gameResultDao(): GameResultDao
    abstract fun localPackDao(): LocalPackDao
    abstract fun localCardDao(): LocalCardDao
    abstract fun savedCardDao(): SavedCardDao
    abstract fun momentDao(): MomentDao

    companion object {
        private const val NAME = "board_game.db"

        /** v4: saved ♥ questions. Non-destructive so custom packs survive. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saved_card` (" +
                        "`id` TEXT NOT NULL, `text` TEXT NOT NULL, `back` TEXT, `level` TEXT, " +
                        "`packId` TEXT, `packTitle` TEXT, `savedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
            }
        }

        /** v5: captured moments + `game_result.sessionId`. Non-destructive (adds only). */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `moment` (" +
                        "`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                        "`filePath` TEXT NOT NULL, `durationMs` INTEGER, `cardText` TEXT, `playerName` TEXT, " +
                        "`packTitle` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("ALTER TABLE `game_result` ADD COLUMN `sessionId` TEXT")
            }
        }

        fun create(context: Context): BoardGameDatabase =
            Room.databaseBuilder(context, BoardGameDatabase::class.java, NAME)
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}
