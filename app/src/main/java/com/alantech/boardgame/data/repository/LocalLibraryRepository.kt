package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.local.entity.LocalCardEntity
import com.alantech.boardgame.data.local.entity.LocalPackEntity
import kotlinx.coroutines.flow.Flow

interface LocalLibraryRepository {
    fun getAllPacks(): Flow<List<LocalPackEntity>>
    fun getCardsForPack(packId: String): Flow<List<LocalCardEntity>>
    suspend fun createPack(pack: LocalPackEntity)
    suspend fun deletePack(pack: LocalPackEntity)
    suspend fun createCard(card: LocalCardEntity)
    suspend fun deleteCard(card: LocalCardEntity)
}
