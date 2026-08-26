package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.SectionEntity
import com.boardgame.deepdeck.data.model.VibeCategory
import com.boardgame.deepdeck.features.home.model.VibeChip

interface HomeDataRepository {
    suspend fun getAllVibesData(): Result<List<VibeChip>>
    suspend fun getCardsWithVibe(categoryId: String): Result<List<PacksPreview>>
    suspend fun getSections(): Result<List<SectionEntity>>
    suspend fun getSectionDetail(sectionID: String): Result<SectionEntity>
    suspend fun getSectionPacks(sectionId: String): Result<List<PacksPreview>>
}