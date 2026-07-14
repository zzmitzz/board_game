package com.alantech.boardgame.data.remote

import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.SectionEntity
import com.alantech.boardgame.data.model.VibeCategory
import com.alantech.boardgame.data.remote.response.BaseResponse
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