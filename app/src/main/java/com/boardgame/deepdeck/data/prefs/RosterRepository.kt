package com.boardgame.deepdeck.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boardgame.deepdeck.features.ingame.model.GameConfig
import com.boardgame.deepdeck.ui.model.GamePlayer
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** Last roster + house rules, restored on the next Setup (plan §7.2.6 "remembered roster"). */
@Serializable
data class RememberedSetup(
    val players: List<GamePlayer> = emptyList(),
    val rounds: Int = GameConfig.DEFAULT_ROUNDS,
    val speedMode: Boolean = false,
    val penaltyEnabled: Boolean = false,
    val penaltyText: String = "",
    /** null = follow the default (on for 3+ players). */
    val passThePhone: Boolean? = null,
    /** Next player id to hand out; ids are never reused. */
    val nextPlayerId: Int = 1,
)

@Singleton
class RosterRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun load(): RememberedSetup {
        val raw = dataStore.data.first()[KEY] ?: return RememberedSetup()
        return runCatching { json.decodeFromString(RememberedSetup.serializer(), raw) }
            .onFailure { android.util.Log.w("RosterRepository", "remembered setup unreadable", it) }
            .getOrDefault(RememberedSetup())
    }

    suspend fun save(setup: RememberedSetup) {
        dataStore.edit { it[KEY] = json.encodeToString(RememberedSetup.serializer(), setup) }
    }

    private companion object {
        val KEY = stringPreferencesKey("pref_remembered_setup")
    }
}
