package com.alantech.boardgame.data.local

import androidx.room.TypeConverter
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.GamePlayer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameResultTypeConverters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromGameScore(value: Map<GamePlayer, GamePlayerScore>): String {
        val list = value.entries.map { GameScoreEntry(it.key, it.value) }
        return json.encodeToString(list)
    }

    @TypeConverter
    fun toGameScore(value: String): Map<GamePlayer, GamePlayerScore> {
        val list = json.decodeFromString<List<GameScoreEntry>>(value)
        return list.associate { it.player to it.score }
    }
}
