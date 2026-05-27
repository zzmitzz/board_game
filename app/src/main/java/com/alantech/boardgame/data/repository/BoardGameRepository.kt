package com.alantech.boardgame.data.repository

import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.CardPreview

interface BoardGameRepository {
    suspend fun getPacks(): List<CardPreview>
    suspend fun getPackById(id: String): CardPreview?
    suspend fun getCardsByPackId(packId: String): List<CardDetail>
}
