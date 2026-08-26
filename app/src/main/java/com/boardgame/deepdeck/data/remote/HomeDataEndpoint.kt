package com.boardgame.deepdeck.data.remote

import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.SectionEntity
import com.boardgame.deepdeck.data.model.VibeCategory
import com.boardgame.deepdeck.data.remote.response.BaseResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeDataEndpoint {
    @GET("/api/v1/vibe-categories")
    suspend fun getAllVibes(): List<VibeCategory>

    @GET("/api/v1/vibe-categories/cards")
    suspend fun getCardsWithVibe(
        @Query("category_id") categoryId: String
    ) : List<PacksPreview>

    @GET("/api/v1/sections")
    suspend fun getSections(): List<SectionEntity>

    @GET("/api/v1/sections/detail")
    suspend fun getSectionDetail(
        @Query("section_id") sectionId: String
    ): SectionEntity

    @GET("/api/v1/sections/packs")
    suspend fun getSectionPacks(
        @Query("section_id") sectionId: String
    ): List<PacksPreview>


}