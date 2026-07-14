package com.alantech.boardgame.data.remote

import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.RemoteCard
import com.alantech.boardgame.data.model.RemotePackDetail
import retrofit2.http.GET
import retrofit2.http.Query

interface BoardGameEndpoint {

    @GET("api/v1/packs")
    suspend fun getPacks(): List<RemotePackDetail>

    @GET("api/v1/packs/detail")
    suspend fun getPackById(
        @Query("pack_id") idQuery: String,
    ): RemotePackDetail

    @GET("api/v1/packs/cards")
    suspend fun getCards(
        @Query("pack_id") packIdQuery: String,
        @Query("lang") languageQuery: String
    ): List<RemoteCard>

    @GET("api/v1/packs/cards/sample")
    suspend fun getSampleCard(
        @Query("pack_id") packIdQuery: String,
        @Query("lang") languageQuery: String
        ): List<RemoteCard>

    @GET("api/v1/packs/suggest")
    suspend fun getSuggestPacks(): List<PacksPreview>

    @GET("api/v1/packs/search/preview")
    suspend fun searchPacksByName(
        @Query("query") query: String,
        @Query("limit") limit: Int = 5
    ): List<PacksPreview>
}