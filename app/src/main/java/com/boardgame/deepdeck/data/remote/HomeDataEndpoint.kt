package com.boardgame.deepdeck.data.remote

import com.boardgame.deepdeck.data.model.DailyCardResponse
import com.boardgame.deepdeck.data.model.HomeFeedResponse
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.SectionEntity
import com.boardgame.deepdeck.data.model.VibeCategory
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeDataEndpoint {

    /** Single-call home feed (new). 404 on older backends → caller falls back. */
    @GET("api/v1/home")
    suspend fun getHome(
        @Query("lang") lang: String,
        @Query("packs_per_section") packsPerSection: Int = 10,
    ): HomeFeedResponse

    @GET("api/v1/daily-card")
    suspend fun getDailyCard(
        @Query("lang") lang: String,
        @Query("date") date: String? = null,
    ): DailyCardResponse

    @GET("api/v1/vibe-categories/")
    suspend fun getAllVibes(
        @Query("lang") lang: String? = null,
    ): List<VibeCategory>

    @GET("api/v1/vibe-categories/cards")
    suspend fun getCardsWithVibe(
        @Query("category_id") categoryId: String
    ): List<PacksPreview>

    @GET("api/v1/sections/")
    suspend fun getSections(): List<SectionEntity>

    @GET("api/v1/sections/detail")
    suspend fun getSectionDetail(
        @Query("section_id") sectionId: String
    ): SectionEntity

    @GET("api/v1/sections/packs")
    suspend fun getSectionPacks(
        @Query("section_id") sectionId: String
    ): List<PacksPreview>
}
