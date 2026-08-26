package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.local.dao.LocalCardDao
import com.boardgame.deepdeck.data.local.dao.LocalPackDao
import com.boardgame.deepdeck.data.local.entity.toCardDetail
import com.boardgame.deepdeck.data.local.entity.toPackDetailUIModel
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.model.PackDetailUIModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CustomPackLocallyRepository @Inject constructor(
    private val packDao: LocalPackDao,
    private val cardDao: LocalCardDao
) : BoardGameRepository {

    override suspend fun getPackById(id: String): PackDetailUIModel? =
        packDao.getAllPacks().first().find { it.id == id }?.toPackDetailUIModel()

    override suspend fun getCardsByPackId(packId: String, language: String): List<CardDetail> =
        cardDao.getCardsForPack(packId).first().map { it.toCardDetail() }

    override suspend fun getPacks(): List<PackDetailUIModel> =
        packDao.getAllPacks().first().map { it.toPackDetailUIModel() }

    override suspend fun getSampleCard(packId: String, language: String): List<CardDetail> =
        cardDao.getCardsForPack(packId).first().take(3).map { it.toCardDetail() }

    override suspend fun getRecentSearch(): List<String> = emptyList()

    override suspend fun saveRecentSearch(search: String) = Unit

    override suspend fun getSuggestPacks(): List<PacksPreview> = emptyList()

    override suspend fun searchPacksByName(query: String): List<PacksPreview> = emptyList()

    override suspend fun translateCards(cardIds: List<String>, locale: String): List<CardDetail> = emptyList()
}
