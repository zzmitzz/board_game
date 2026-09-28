package com.boardgame.deepdeck.utils

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Image picker for custom packs/cards. Uses OpenDocument + takePersistableUriPermission so the
 * stored content:// URI stays readable after a reboot / app restart (GetContent grants are
 * temporary). Returns a launcher lambda.
 */
@Composable
fun rememberPersistentImagePicker(onPicked: (Uri?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        onPicked(uri)
    }
    return { launcher.launch(arrayOf("image/*")) }
}
