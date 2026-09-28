package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.local.entity.LocalCardEntity
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import kotlinx.coroutines.flow.Flow

interface LocalLibraryRepository {
    fun getAllPacks(): Flow<List<LocalPackEntity>>
    fun getCardsForPack(packId: String): Flow<List<LocalCardEntity>>
    /** packId → number of cards. */
    fun observeCardCounts(): Flow<Map<String, Int>>
    suspend fun createPack(pack: LocalPackEntity)
    suspend fun deletePack(pack: LocalPackEntity)
    suspend fun createCard(card: LocalCardEntity)
    suspend fun deleteCard(card: LocalCardEntity)
}
