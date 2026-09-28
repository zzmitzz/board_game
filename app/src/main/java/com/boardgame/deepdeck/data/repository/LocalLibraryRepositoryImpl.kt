package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.local.dao.LocalCardDao
import com.boardgame.deepdeck.data.local.dao.LocalPackDao
import com.boardgame.deepdeck.data.local.entity.LocalCardEntity
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalLibraryRepositoryImpl @Inject constructor(
    private val packDao: LocalPackDao,
    private val cardDao: LocalCardDao
) : LocalLibraryRepository {

    override fun getAllPacks(): Flow<List<LocalPackEntity>> = packDao.getAllPacks()

    override fun getCardsForPack(packId: String): Flow<List<LocalCardEntity>> =
        cardDao.getCardsForPack(packId)

    override fun observeCardCounts(): Flow<Map<String, Int>> =
        cardDao.observeCardCounts().map { rows -> rows.associate { it.packId to it.count } }

    override suspend fun createPack(pack: LocalPackEntity) = packDao.insert(pack)

    override suspend fun deletePack(pack: LocalPackEntity) = packDao.delete(pack)

    override suspend fun createCard(card: LocalCardEntity) = cardDao.insert(card)

    override suspend fun deleteCard(card: LocalCardEntity) = cardDao.delete(card)
}
