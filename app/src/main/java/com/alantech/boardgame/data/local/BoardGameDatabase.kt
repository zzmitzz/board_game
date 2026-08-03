package com.alantech.boardgame.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alantech.boardgame.data.local.dao.GameResultDao
import com.alantech.boardgame.data.local.entity.GameResult

@Database(entities = [GameResult::class], version = 2, exportSchema = false)
@TypeConverters(GameResultTypeConverters::class)
abstract class BoardGameDatabase : RoomDatabase() {

    abstract fun gameResultDao(): GameResultDao

    companion object {
        private const val NAME = "board_game.db"

        fun create(context: Context): BoardGameDatabase =
            Room.databaseBuilder(context, BoardGameDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}
