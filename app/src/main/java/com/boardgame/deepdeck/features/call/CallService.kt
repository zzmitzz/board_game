package com.boardgame.deepdeck.features.call

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.boardgame.deepdeck.MainActivity
import com.boardgame.deepdeck.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Foreground service for the duration of a call: the ongoing notification, and the reason Android
 * lets camera + mic keep running while the user checks another app. The call itself lives in
 * CallVM; "Leave" in the notification reaches it through [hangUpRequests].
 */
class CallService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_HANG_UP) {
            _hangUpRequests.tryEmit(Unit)
            return START_NOT_STICKY
        }
        val room = intent?.getStringExtra(EXTRA_ROOM).orEmpty()
        createChannel()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceCompat.startForeground(this, NOTIFICATION_ID, notification(room), serviceTypes())
            } else {
                startForeground(NOTIFICATION_ID, notification(room))
            }
        } catch (e: RuntimeException) {
            // e.g. ForegroundServiceStartNotAllowedException when started from the background.
            Log.w(TAG, "Could not go foreground", e)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    /** Only the types whose permission is granted (API 34 refuses the others). CallVM starts the
     *  service only with the mic or the camera granted, so this is never 0. */
    private fun serviceTypes(): Int {
        var types = 0
        if (granted(Manifest.permission.RECORD_AUDIO)) types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        if (granted(Manifest.permission.CAMERA)) types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        return types
    }

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun notification(room: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(getString(R.string.call_notification_title))
        .setContentText(getString(R.string.call_notification_text, room))
        .setOngoing(true)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                // Back to the running MainActivity (and its call screen), not a new one.
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .addAction(
            0,
            getString(R.string.call_leave),
            PendingIntent.getService(
                this, 1,
                Intent(this, CallService::class.java).setAction(ACTION_HANG_UP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.call_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "CallService"
        private const val CHANNEL_ID = "live_call"
        private const val NOTIFICATION_ID = 3000
        private const val EXTRA_ROOM = "room"
        private const val ACTION_HANG_UP = "com.boardgame.deepdeck.call.HANG_UP"

        private val _hangUpRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val hangUpRequests: SharedFlow<Unit> = _hangUpRequests.asSharedFlow()

        fun start(context: Context, room: String) {
            val intent = Intent(context, CallService::class.java).putExtra(EXTRA_ROOM, room)
            runCatching { ContextCompat.startForegroundService(context, intent) }
                .onFailure { Log.w(TAG, "Could not start the call service", it) }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CallService::class.java))
        }
    }
}
