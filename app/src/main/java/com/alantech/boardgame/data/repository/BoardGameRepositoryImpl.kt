package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.toUIModel
import com.alantech.boardgame.data.remote.BoardGameEndpoint
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.PackDetailUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BoardGameRepositoryImpl(
    private val api: BoardGameEndpoint
) : BoardGameRepository {

    private val searchHistory = mutableListOf<String>()

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
        return searchHistory.subList(0, searchHistory.size.coerceAtMost(5))
    }

    override suspend fun saveRecentSearch(search: String) {
        searchHistory.add(search)
    }

    override suspend fun getSuggestPacks(): List<PacksPreview> = api.getSuggestPacks()

    override suspend fun searchPacksByName(query: String): List<PacksPreview> = api.searchPacksByName(query)
}
