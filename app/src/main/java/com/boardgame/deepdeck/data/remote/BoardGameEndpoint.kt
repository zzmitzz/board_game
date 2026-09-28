package com.boardgame.deepdeck.data.remote

import com.boardgame.deepdeck.data.model.CardFeedbackRequest
import com.boardgame.deepdeck.data.model.GameSessionCreate
import com.boardgame.deepdeck.data.model.IdResponse
import com.boardgame.deepdeck.data.model.OkResponse
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.RemoteCard
import com.boardgame.deepdeck.data.model.RemotePackDetail
import com.boardgame.deepdeck.data.remote.request.CardTranslateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BoardGameEndpoint {

    @GET("api/v1/packs/")
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

    @POST("api/v1/cards/translate")
    suspend fun translateCards(
        @Body cardsData: CardTranslateRequest
    ): List<RemoteCard>

    /** Fire-and-forget reaction (LOVE | SKIP | REPORT); idempotent per device/card/reaction. */
    @POST("api/v1/cards/feedback")
    suspend fun sendCardFeedback(
        @Body body: CardFeedbackRequest
    ): OkResponse

    /** Analytics: one row per finished game; also bumps packs.play_count. */
    @POST("api/v1/events/sessions")
    suspend fun sendGameSession(
        @Body body: GameSessionCreate
    ): IdResponse
}
