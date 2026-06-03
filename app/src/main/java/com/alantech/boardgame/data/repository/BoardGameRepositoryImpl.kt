package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.model.toUIModel
import com.alantech.boardgame.data.remote.BoardGameAPI
import com.alantech.boardgame.data.remote.BoardGameRepository
import com.alantech.boardgame.data.remote.request.SampleCardRequest
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.CardPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BoardGameRepositoryImpl(
    private val api: BoardGameAPI
) : BoardGameRepository {

    override suspend fun getPacks(): List<CardPreview> = withContext(Dispatchers.IO) {
        api.getPacks().map { it.toUIModel() }
    }

    override suspend fun getPackById(id: String): CardPreview? = withContext(Dispatchers.IO) {
        api.getPackById(idQuery = "eq.$id").firstOrNull()?.toUIModel()
    }

    override suspend fun getCardsByPackId(packId: String): List<CardDetail> = withContext(Dispatchers.IO) {
        api.getCards(packIdQuery = "eq.$packId").map { it.toUIModel() }
    }

    override suspend fun getSampleCard(packId: String): List<CardDetail> = withContext(Dispatchers.IO) {
        api.getSampleCard(SampleCardRequest(packId)).cards.map { it.toUIModel() }
    }
}
