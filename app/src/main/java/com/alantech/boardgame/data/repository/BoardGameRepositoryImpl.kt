package com.alantech.boardgame.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.toUIModel
import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.PackDetailUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val KEY_RECENT_SEARCH = stringPreferencesKey("recent_search_list")
private const val RECENT_SEARCH_DELIMITER = "|||"
private const val MAX_RECENT_SEARCH = 5

class BoardGameRepositoryImpl(
    private val api: BoardGameEndpoint,
    private val dataStore: DataStore<Preferences>
) : BoardGameRepository {

    override suspend fun getPacks(): List<PackDetailUIModel> = withContext(Dispatchers.IO) {
        api.getPacks().map { it.toUIModel() }
    }

    override suspend fun getPackById(id: String): PackDetailUIModel = withContext(Dispatchers.IO) {
        api.getPackById(idQuery = id).toUIModel()
    }

    override suspend fun getCardsByPackId(packId: String, language: String): List<CardDetail> = withContext(Dispatchers.IO) {
        api.getCards(packIdQuery = packId, languageQuery = language).map { it.toUIModel() }
    }

    override suspend fun getSampleCard(packId: String, language: String): List<CardDetail> = withContext(Dispatchers.IO) {
        api.getSampleCard(packId, language).map { it.toUIModel() }
    }

    override suspend fun getRecentSearch(): List<String> {
        return dataStore.data
            .map { prefs -> prefs[KEY_RECENT_SEARCH].orEmpty() }
            .firstOrNull()
            ?.split(RECENT_SEARCH_DELIMITER)
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    override suspend fun saveRecentSearch(search: String) {
        val trimmed = search.trim()
        if (trimmed.isBlank()) return
        dataStore.edit { prefs ->
            val current = prefs[KEY_RECENT_SEARCH].orEmpty()
                .split(RECENT_SEARCH_DELIMITER)
                .filter { it.isNotBlank() }
                .toMutableList()
            current.remove(trimmed)
            current.add(0, trimmed)
            prefs[KEY_RECENT_SEARCH] = current
                .take(MAX_RECENT_SEARCH)
                .joinToString(RECENT_SEARCH_DELIMITER)
        }
    }

    override suspend fun getSuggestPacks(): List<PacksPreview> = api.getSuggestPacks()

    override suspend fun searchPacksByName(query: String): List<PacksPreview> = api.searchPacksByName(query)
}
