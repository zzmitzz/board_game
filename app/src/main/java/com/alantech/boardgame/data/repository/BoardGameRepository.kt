package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.PackDetailUIModel

interface BoardGameRepository {
    suspend fun getPacks(): List<PackDetailUIModel>
    suspend fun getPackById(id: String): PackDetailUIModel?
    suspend fun getCardsByPackId(packId: String): List<CardDetail>
    suspend fun getSampleCard(packId: String): List<CardDetail>
    suspend fun getRecentSearch(): List<String>
    suspend fun saveRecentSearch(search: String)
    suspend fun getSuggestPacks(): List<PacksPreview>
    suspend fun searchPacksByName(query: String) : List<PacksPreview>
}