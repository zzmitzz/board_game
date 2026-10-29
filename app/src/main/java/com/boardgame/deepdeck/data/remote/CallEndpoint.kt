package com.boardgame.deepdeck.data.remote

import com.boardgame.deepdeck.data.model.RemoteRoomCreated
import com.boardgame.deepdeck.data.model.RemoteRoomInfo
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Live call rooms. The call itself runs over the room's WebSocket (see SignalingClient). */
interface CallEndpoint {

    @POST("api/v1/calls/rooms")
    suspend fun createRoom(): RemoteRoomCreated

    @GET("api/v1/calls/rooms/{code}")
    suspend fun getRoom(@Path("code") code: String): RemoteRoomInfo
}
