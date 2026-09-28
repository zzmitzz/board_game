package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.model.DailyCardResponse
import com.boardgame.deepdeck.data.model.HomeSectionDto
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.SectionEntity
import com.boardgame.deepdeck.data.model.VibeCategory
import kotlinx.serialization.Serializable

/** Home content, from `GET /home` or assembled from the legacy endpoints. */
@Serializable
data class HomeFeed(
    val dailyCard: DailyCardResponse? = null,
    val vibes: List<VibeCategory> = emptyList(),
    val sections: List<HomeSectionDto> = emptyList(),
    /** True when served from the on-device cache (offline / failed refresh). */
    val fromCache: Boolean = false,
)

interface HomeDataRepository {
    /** Network fetch; falls back to legacy endpoints when `/home` is missing. Caches on success. */
    suspend fun getHomeFeed(lang: String): Result<HomeFeed>

    /** Last successful feed, or null. */
    suspend fun getCachedHomeFeed(): HomeFeed?

    suspend fun getDailyCard(lang: String): Result<DailyCardResponse?>
    suspend fun getVibes(lang: String): Result<List<VibeCategory>>
    suspend fun getCardsWithVibe(categoryId: String): Result<List<PacksPreview>>
    suspend fun getSections(): Result<List<SectionEntity>>
    suspend fun getSectionDetail(sectionID: String): Result<SectionEntity>
    suspend fun getSectionPacks(sectionId: String): Result<List<PacksPreview>>
}
