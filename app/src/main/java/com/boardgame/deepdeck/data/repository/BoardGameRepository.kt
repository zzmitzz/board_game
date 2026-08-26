package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.PackDetailUIModel

interface BoardGameRepository {
    suspend fun getPacks(): List<PackDetailUIModel>
    suspend fun getPackById(id: String): PackDetailUIModel?
    suspend fun getCardsByPackId(packId: String, language: String): List<CardDetail>
    suspend fun getSampleCard(packId: String, language: String): List<CardDetail>
    suspend fun getRecentSearch(): List<String>
    suspend fun saveRecentSearch(search: String)
    suspend fun getSuggestPacks(): List<PacksPreview>
    suspend fun searchPacksByName(query: String): List<PacksPreview>
    suspend fun translateCards(cardIds: List<String>, locale: String): List<CardDetail>
}