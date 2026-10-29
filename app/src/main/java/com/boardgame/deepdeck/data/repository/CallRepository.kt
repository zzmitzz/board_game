package com.boardgame.deepdeck.data.repository

import com.boardgame.deepdeck.data.remote.CallEndpoint
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Why a room could not be opened or joined; the UI maps each to a message. */
enum class RoomProblem { NOT_FOUND, FULL, OFFLINE, SERVER }

sealed interface RoomResult {
    data class Ready(val code: String) : RoomResult
    data class Failed(val problem: RoomProblem) : RoomResult
}

@Singleton
class CallRepository @Inject constructor(
    private val endpoint: CallEndpoint,
) {
    suspend fun createRoom(): RoomResult = attempt { RoomResult.Ready(endpoint.createRoom().code) }

    /** Checks a typed code before the lobby opens the camera. */
    suspend fun checkRoom(code: String): RoomResult = attempt {
        val room = endpoint.getRoom(code)
        when {
            room.joinable -> RoomResult.Ready(room.code)
            room.participants >= room.capacity -> RoomResult.Failed(RoomProblem.FULL)
            else -> RoomResult.Failed(RoomProblem.NOT_FOUND)
        }
    }

    private inline fun attempt(block: () -> RoomResult): RoomResult = try {
        block()
    } catch (e: HttpException) {
        RoomResult.Failed(if (e.code() == 404) RoomProblem.NOT_FOUND else RoomProblem.SERVER)
    } catch (_: IOException) {
        RoomResult.Failed(RoomProblem.OFFLINE)
    } catch (_: kotlinx.serialization.SerializationException) {
        RoomResult.Failed(RoomProblem.SERVER)
    }
}
