package com.boardgame.deepdeck.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.boardgame.deepdeck.data.local.dao.GameResultDao
import com.boardgame.deepdeck.data.local.dao.LocalCardDao
import com.boardgame.deepdeck.data.local.dao.LocalPackDao
import com.boardgame.deepdeck.data.local.dao.SavedCardDao
import com.boardgame.deepdeck.data.local.entity.GameResult
import com.boardgame.deepdeck.data.local.entity.LocalCardEntity
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [GameResult::class, LocalPackEntity::class, LocalCardEntity::class, SavedCardEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(GameResultTypeConverters::class)
abstract class BoardGameDatabase : RoomDatabase() {

    abstract fun gameResultDao(): GameResultDao
    abstract fun localPackDao(): LocalPackDao
    abstract fun localCardDao(): LocalCardDao
    abstract fun savedCardDao(): SavedCardDao

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

        fun create(context: Context): BoardGameDatabase =
            Room.databaseBuilder(context, BoardGameDatabase::class.java, NAME)
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}
