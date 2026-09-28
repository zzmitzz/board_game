package com.boardgame.deepdeck.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.boardgame.deepdeck.work.EveningReminder

/**
 * Returns a lambda that asks for POST_NOTIFICATIONS (API 33+) when needed and reports whether
 * the app may notify. Below API 33 (or when already granted) it answers immediately.
 */
@Composable
fun rememberNotificationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        callback(granted && EveningReminder.canNotify(context))
    }
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && EveningReminder.needsPermissionRequest(context)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            callback(EveningReminder.canNotify(context))
        }
    }
}

/** Opens the app's notification settings (when the user denied the permission before). */
fun Context.openNotificationSettings() {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.parse("package:$packageName"))
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}
