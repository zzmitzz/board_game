package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.SectionEntity
import com.alantech.boardgame.data.model.VibeCategory
import com.alantech.boardgame.data.remote.HomeDataEndpoint
import com.alantech.boardgame.data.remote.response.BaseResponse
import com.alantech.boardgame.features.home.model.VibeChip
import com.alantech.boardgame.features.home.model.toVibeChip
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeDataRepositoryImpl(
    private val apiHomeData: HomeDataEndpoint
) : HomeDataRepository {
    override suspend fun getAllVibesData(): Result<List<VibeChip>> {
        return withContext(Dispatchers.IO){
            return@withContext runCatching {
                try {
                    val response = apiHomeData.getAllVibes()
                    response.map { it.toVibeChip() }
                }catch (e: CancellationException){
                    throw e
                }catch (e: Exception){
                    throw Exception(e.message)
                }
            }
        }
    }

    override suspend fun getCardsWithVibe(categoryId: String): Result<List<PacksPreview>> {
        return runCatching {
            try {
                apiHomeData.getCardsWithVibe(categoryId)
            }catch (e: CancellationException){
                throw e
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    override suspend fun getSections(): Result<List<SectionEntity>> {
        return runCatching {
            try {
                apiHomeData.getSections()
            }catch (e: CancellationException){
                throw e
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }

    override suspend fun getSectionDetail(sectionID: String): Result<SectionEntity> {
        return runCatching {
            try {
                apiHomeData.getSectionDetail(sectionID)
            }catch (e: CancellationException){
                throw e
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }

    }

    override suspend fun getSectionPacks(sectionId: String): Result<List<PacksPreview>> {
        return runCatching {
            try {
                apiHomeData.getSectionPacks(sectionId)
            }catch (e: CancellationException){
                throw e
            }catch (e: Exception){
                throw Exception(e.message)
            }
        }
    }


}