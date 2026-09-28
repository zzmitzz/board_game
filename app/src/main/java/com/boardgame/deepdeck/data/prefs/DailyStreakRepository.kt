package com.boardgame.deepdeck.data.prefs

import androidx.datastore.preferences.core.stringPreferencesKey
import com.boardgame.deepdeck.utils.DataStoreUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/** Persisted streak (Gson via DataStoreUtils). Days are local epoch days. */
data class StreakRecord(
    val current: Int = 0,
    val best: Int = 0,
    val lastRevealEpochDay: Long? = null,
)

/** What the UI shows: [current] is already 0 when the streak is broken. */
data class StreakState(
    val current: Int = 0,
    val best: Int = 0,
    val revealedToday: Boolean = false,
)

/**
 * Tonight's Card streak (F1). Revealing the daily card once per local day
 * increments the streak; a gap of more than one day resets it to 1.
 */
@Singleton
class DailyStreakRepository @Inject constructor(
    private val dataStoreUtils: DataStoreUtils,
) {
    private val mutex = Mutex()

    val streak: Flow<StreakState> = dataStoreUtils.getFlow(KEY, StreakRecord::class.java)
        .map { (it ?: StreakRecord()).toState(todayEpochDay()) }

    /** Registers today's reveal; idempotent within the same day. */
    suspend fun registerReveal(today: Long = todayEpochDay()): StreakState = mutex.withLock {
        val record = dataStoreUtils.getSerializedData(KEY, StreakRecord::class.java) ?: StreakRecord()
        val last = record.lastRevealEpochDay
        if (last == today) return@withLock record.toState(today)
        val current = if (last != null && today - last == 1L) record.current + 1 else 1
        val updated = StreakRecord(
            current = current,
            best = maxOf(record.best, current),
            lastRevealEpochDay = today
        )
        dataStoreUtils.setSerializedData(KEY, updated)
        updated.toState(today)
    }

    private fun StreakRecord.toState(today: Long): StreakState {
        val last = lastRevealEpochDay
        val alive = last != null && today - last <= 1L
        return StreakState(
            current = if (alive) current else 0,
            best = best,
            revealedToday = last == today
        )
    }

    companion object {
        private val KEY = stringPreferencesKey("pref_daily_streak")

        /** Local-time epoch day (API 25 safe; no java.time). */
        fun todayEpochDay(now: Long = System.currentTimeMillis()): Long {
            val offset = TimeZone.getDefault().getOffset(now)
            return Math.floorDiv(now + offset, 86_400_000L)
        }
    }
}
