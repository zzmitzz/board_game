package com.alantech.boardgame.data.remote

import com.alantech.boardgame.data.model.RemoteCard
import com.alantech.boardgame.data.model.RemotePack
import retrofit2.http.GET
import retrofit2.http.Query

interface BoardGameAPI {

    @GET("rest/v1/packs")
    suspend fun getPacks(
        @Query("select") select: String = "*"
    ): List<RemotePack>

    @GET("rest/v1/packs")
    suspend fun getPackById(
        @Query("_id") idQuery: String,
        @Query("select") select: String = "*"
    ): List<RemotePack>

    @GET("rest/v1/cards")
    suspend fun getCards(
        @Query("pack_id") packIdQuery: String,
        @Query("select") select: String = "*"
    ): List<RemoteCard>
}