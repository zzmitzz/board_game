package com.boardgame.deepdeck.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens the system share sheet with plain text. */
fun Context.shareText(text: String, chooserTitle: String? = null) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
    }
}

/**
 * Shares a rendered image (content:// from the app FileProvider) with optional caption text.
 * Falls back to [fallbackText] as plain text when [uri] is null (rendering failed).
 */
fun Context.shareImage(uri: Uri?, text: String? = null, chooserTitle: String? = null, fallbackText: String? = null) {
    if (uri == null) {
        (fallbackText ?: text)?.let { shareText(it, chooserTitle) }
        return
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        text?.let { putExtra(Intent.EXTRA_TEXT, it) }
        clipData = android.content.ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        startActivity(
            Intent.createChooser(send, chooserTitle)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    } catch (_: ActivityNotFoundException) {
    }
}

/** Opens the Play Store listing (market:// first, web fallback). */
fun Context.openPlayStoreListing() {
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(market)
    } catch (_: ActivityNotFoundException) {
        openWebPage("https://play.google.com/store/apps/details?id=$packageName")
    }
}
