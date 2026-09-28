package com.boardgame.deepdeck.data.repository

import android.util.Log
import com.boardgame.deepdeck.data.local.dao.SavedCardDao
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import com.boardgame.deepdeck.data.model.CardFeedbackRequest
import com.boardgame.deepdeck.data.prefs.DeviceIdRepository
import com.boardgame.deepdeck.data.remote.BoardGameEndpoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

enum class CardReaction { LOVE, SKIP, REPORT }

/**
 * Saved ♥ questions (Room `saved_card`) + anonymous card feedback
 * (`POST /cards/feedback`). Feedback is best-effort: failures are swallowed.
 */
@Singleton
class SavedCardRepository @Inject constructor(
    private val dao: SavedCardDao,
    private val api: BoardGameEndpoint,
    private val deviceIdRepository: DeviceIdRepository,
) {
    fun observeAll(): Flow<List<SavedCardEntity>> = dao.observeAll()

    fun observeIsSaved(cardId: String): Flow<Boolean> = dao.observeIsSaved(cardId)

    suspend fun save(card: SavedCardEntity, sendLove: Boolean = true) {
        dao.insert(card)
        if (sendLove) sendFeedback(card.id, card.packId, CardReaction.LOVE)
    }

    suspend fun remove(cardId: String) = dao.deleteById(cardId)

    /** Fire-and-forget reaction; never throws (except cancellation). */
    suspend fun sendFeedback(cardId: String, packId: String?, reaction: CardReaction) {
        try {
            api.sendCardFeedback(
                CardFeedbackRequest(
                    deviceId = deviceIdRepository.get(),
                    cardId = cardId,
                    packId = packId,
                    reaction = reaction.name
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "card feedback failed: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "SavedCardRepository"
    }
}
