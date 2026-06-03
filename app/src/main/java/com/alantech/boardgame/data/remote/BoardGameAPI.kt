package com.alantech.boardgame.data.remote

import com.alantech.boardgame.data.model.RemoteCard
import com.alantech.boardgame.data.model.RemotePack
import com.alantech.boardgame.data.remote.request.SampleCardRequest
import com.alantech.boardgame.data.remote.response.SampleCardResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BoardGameAPI {

    @GET("rest/v1/packs")
    suspend fun getPacks(
        @Query("select") select: String = "*"
    ): List<RemotePack>

    @GET("rest/v1/packs")
    suspend fun getPackById(
        @Query("id") idQuery: String,
        @Query("select") select: String = "*"
    ): List<RemotePack>

    @GET("rest/v1/cards")
    suspend fun getCards(
        @Query("pack_id") packIdQuery: String,
        @Query("select") select: String = "*"
    ): List<RemoteCard>

    @POST("functions/v1/get_sample_card_for_pack")
    suspend fun getSampleCard(
        @Body data: SampleCardRequest
    ): SampleCardResponse
}