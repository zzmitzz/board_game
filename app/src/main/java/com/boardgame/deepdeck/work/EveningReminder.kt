package com.boardgame.deepdeck.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.boardgame.deepdeck.R
import dagger.hilt.android.EntryPointAccessors
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Evening reminder (plan F12 / §7.2.12): opt-in, daily at 20:00 local, "Tonight's card is ready".
 * Implemented as a self-rescheduling one-time work (no periodic drift); each run schedules the next.
 */
object EveningReminder {
    const val CHANNEL_ID = "evening_reminder"
    private const val WORK_NAME = "evening_reminder"
    private const val NOTIFICATION_ID = 2000
    const val HOUR = 20

    /** Creates the notification channel (no-op below API 26). Safe to call repeatedly. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.reminder_channel_desc) }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    /** Whether we may post notifications (runtime permission on API 33+, and not blocked). */
    fun canNotify(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** True when the app must ask for POST_NOTIFICATIONS before enabling the reminder. */
    fun needsPermissionRequest(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED

    /**
     * Schedules the next 20:00 run. [replace] = true when the user just toggled it on;
     * false (KEEP) on app start so an already queued run is left alone.
     */
    fun schedule(context: Context, replace: Boolean) {
        enqueue(context, if (replace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    internal fun enqueue(context: Context, policy: ExistingWorkPolicy) {
        val request = OneTimeWorkRequestBuilder<EveningReminderWorker>()
            .setInitialDelay(millisUntilNext(HOUR), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
    }

    /** Milliseconds until the next [hour]:00 local time (tomorrow if already past). */
    fun millisUntilNext(hour: Int, now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now + 60_000L) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis - now
    }

    internal fun show(context: Context) {
        if (!canNotify(context)) return
        createChannel(context)
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            ?: return
        val pending = PendingIntent.getActivity(
            context, 0, launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFFB57BFF.toInt())
            .setContentTitle(context.getString(R.string.reminder_title))
            .setContentText(context.getString(R.string.reminder_body))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminder_body)))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        }
    }
}

class EveningReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = EntryPointAccessors
            .fromApplication(applicationContext, WorkerEntryPoint::class.java)
            .appSettingsRepository()
        if (!settings.current().notificationsEnabled) return Result.success()

        // Doze can delay a run; never nag in the morning.
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= EveningReminder.HOUR - 1) EveningReminder.show(applicationContext)

        // Queue tomorrow's run after this one completes.
        EveningReminder.enqueue(applicationContext, ExistingWorkPolicy.APPEND_OR_REPLACE)
        return Result.success()
    }
}
