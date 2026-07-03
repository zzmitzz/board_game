package com.alantech.boardgame.data.repository

import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.SectionEntity
import com.alantech.boardgame.data.model.VibeCategory
import com.alantech.boardgame.features.home.model.VibeChip

interface HomeDataRepository {
    suspend fun getAllVibesData(): Result<List<VibeChip>>
    suspend fun getCardsWithVibe(categoryId: String): Result<List<PacksPreview>>
    suspend fun getSections(): Result<List<SectionEntity>>
    suspend fun getSectionPacks(sectionId: String): Result<List<PacksPreview>>
}