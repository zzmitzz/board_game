package com.boardgame.deepdeck.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anonymous install identifier (no login, plan §1.5). Generated once, kept in DataStore.
 * Used for `/cards/feedback` and `/events/sessions`.
 */
@Singleton
class DeviceIdRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()
    @Volatile private var cached: String? = null

    suspend fun get(): String {
        cached?.let { return it }
        return mutex.withLock {
            cached ?: run {
                val existing = dataStore.data.first()[KEY]
                val id = existing ?: UUID.randomUUID().toString().also { newId ->
                    dataStore.edit { it[KEY] = newId }
                }
                cached = id
                id
            }
        }
    }

    private companion object {
        val KEY = stringPreferencesKey("pref_device_id")
    }
}
