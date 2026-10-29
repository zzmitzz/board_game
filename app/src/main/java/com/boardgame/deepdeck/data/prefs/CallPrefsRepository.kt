package com.boardgame.deepdeck.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** The name shown to the other person in a live call, remembered for the next lobby. */
@Singleton
class CallPrefsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun displayName(): String = dataStore.data.first()[KEY_NAME].orEmpty()

    suspend fun saveDisplayName(name: String) {
        dataStore.edit { it[KEY_NAME] = name.trim() }
    }

    private companion object {
        val KEY_NAME = stringPreferencesKey("pref_call_display_name")
    }
}
