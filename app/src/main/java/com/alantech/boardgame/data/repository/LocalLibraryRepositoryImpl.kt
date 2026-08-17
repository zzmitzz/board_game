package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.local.dao.LocalCardDao
import com.alantech.boardgame.data.local.dao.LocalPackDao
import com.alantech.boardgame.data.local.entity.LocalCardEntity
import com.alantech.boardgame.data.local.entity.LocalPackEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalLibraryRepositoryImpl @Inject constructor(
    private val packDao: LocalPackDao,
    private val cardDao: LocalCardDao
) : LocalLibraryRepository {

    override fun getAllPacks(): Flow<List<LocalPackEntity>> = packDao.getAllPacks()

    override fun getCardsForPack(packId: String): Flow<List<LocalCardEntity>> =
        cardDao.getCardsForPack(packId)

    override suspend fun createPack(pack: LocalPackEntity) = packDao.insert(pack)

    override suspend fun deletePack(pack: LocalPackEntity) = packDao.delete(pack)

    override suspend fun createCard(card: LocalCardEntity) = cardDao.insert(card)

    override suspend fun deleteCard(card: LocalCardEntity) = cardDao.delete(card)
}
