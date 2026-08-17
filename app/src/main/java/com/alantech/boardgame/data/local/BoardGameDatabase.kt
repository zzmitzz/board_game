package com.alantech.boardgame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alantech.boardgame.data.local.dao.GameResultDao
import com.alantech.boardgame.data.local.dao.LocalCardDao
import com.alantech.boardgame.data.local.dao.LocalPackDao
import com.alantech.boardgame.data.local.entity.GameResult
import com.alantech.boardgame.data.local.entity.LocalCardEntity
import com.alantech.boardgame.data.local.entity.LocalPackEntity

@Database(
    entities = [GameResult::class, LocalPackEntity::class, LocalCardEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(GameResultTypeConverters::class)
abstract class BoardGameDatabase : RoomDatabase() {

    abstract fun gameResultDao(): GameResultDao
    abstract fun localPackDao(): LocalPackDao
    abstract fun localCardDao(): LocalCardDao

    companion object {
        private const val NAME = "board_game.db"

        fun create(context: Context): BoardGameDatabase =
            Room.databaseBuilder(context, BoardGameDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}
