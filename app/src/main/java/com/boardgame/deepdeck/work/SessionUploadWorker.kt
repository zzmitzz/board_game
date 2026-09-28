package com.boardgame.deepdeck.work

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.boardgame.deepdeck.data.model.GameSessionCreate
import dagger.hilt.android.EntryPointAccessors
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

/**
 * `POST /events/sessions` (plan §7.2.9): fire-and-forget from the game, queued by WorkManager
 * until the device is online, retried with backoff on 5xx / network errors.
 */
class SessionUploadWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val payload = inputData.getString(KEY_PAYLOAD) ?: return Result.failure()
        val deps = EntryPointAccessors.fromApplication(applicationContext, WorkerEntryPoint::class.java)
        return try {
            val body = deps.json().decodeFromString(GameSessionCreate.serializer(), payload)
            deps.boardGameEndpoint().sendGameSession(body)
            Result.success()
        } catch (e: HttpException) {
            // 4xx will never succeed (bad payload / unknown pack); drop it. 429 = rate limited: retry later.
            if (e.code() in 400..499 && e.code() != 429) Result.failure() else retryOrGiveUp()
        } catch (e: Exception) {
            Log.w(TAG, "session upload failed: ${e.message}")
            retryOrGiveUp()
        }
    }

    private fun retryOrGiveUp(): Result = if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()

    companion object {
        private const val TAG = "SessionUploadWorker"
        private const val KEY_PAYLOAD = "payload"
        private const val MAX_ATTEMPTS = 8

        fun enqueue(context: Context, payloadJson: String) {
            val request = OneTimeWorkRequestBuilder<SessionUploadWorker>()
                .setInputData(workDataOf(KEY_PAYLOAD to payloadJson))
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(TAG)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
