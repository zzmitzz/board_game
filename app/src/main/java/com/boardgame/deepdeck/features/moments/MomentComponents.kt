package com.boardgame.deepdeck.features.moments

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.entity.MomentEntity
import com.boardgame.deepdeck.data.local.entity.MomentType
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import java.io.File

val MomentType.icon: ImageVector
    get() = when (this) {
        MomentType.PHOTO -> Icons.Rounded.PhotoCamera
        MomentType.VIDEO -> Icons.Rounded.Videocam
        MomentType.AUDIO -> Icons.Rounded.Mic
    }

@get:StringRes
val MomentType.label: Int
    get() = when (this) {
        MomentType.PHOTO -> R.string.moment_photo
        MomentType.VIDEO -> R.string.moment_video
        MomentType.AUDIO -> R.string.moment_voice
    }

@Composable
fun MomentType.accent(): Color = when (this) {
    MomentType.PHOTO -> DeepTalkTheme.colors.sky
    MomentType.VIDEO -> DeepTalkTheme.colors.rose
    MomentType.AUDIO -> DeepTalkTheme.colors.mint
}

/** Same resolution as `MomentRepository.fileOf`: paths are relative to `filesDir`. */
fun MomentEntity.localFile(context: Context): File = File(context.filesDir, filePath)

/** `m:ss` for clip lengths and the recorder timer. */
fun formatClipDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    return "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
}

/** "Oct 10, 21:03" in the device locale. */
fun formatMomentDate(context: Context, millis: Long): String = DateUtils.formatDateTime(
    context,
    millis,
    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH
)

/**
 * Moment tile: photo / first video frame (Coil) or a voice tile with its length, a type badge
 * in the corner, and a broken-file fallback when the media is gone.
 */
@Composable
fun MomentThumbnail(
    moment: MomentEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = DeepTalkTheme.colors
    val type = moment.momentType
    val accent = type.accent()
    val file = remember(moment.filePath) { moment.localFile(context) }
    var failed by remember(moment.filePath) { mutableStateOf(false) }
    val description = stringResource(
        R.string.moment_tile_desc,
        stringResource(type.label),
        formatMomentDate(context, moment.createdAt)
    )
    Box(
        modifier
            .clip(DeepTalkShapes.md)
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.22f), colors.surface)))
            .border(1.dp, colors.outline, DeepTalkShapes.md)
            .pressable(pressedScale = 0.96f, onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        when {
            failed -> Icon(Icons.Rounded.BrokenImage, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(32.dp))
            type == MomentType.AUDIO -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape)
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null, tint = accent)
                }
                moment.durationMs?.let {
                    Text(
                        formatClipDuration(it),
                        style = DeepTalkTheme.type.label,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            else -> {
                val request = remember(file) {
                    ImageRequest.Builder(context)
                        .data(file)
                        .crossfade(true)
                        .apply {
                            if (type == MomentType.VIDEO) {
                                decoderFactory(VideoFrameDecoder.Factory())
                                videoFrameMillis(VIDEO_FRAME_MS)
                            }
                        }
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = { failed = true },
                    modifier = Modifier.fillMaxSize()
                )
                if (type == MomentType.VIDEO) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
        MomentTypeBadge(type, Modifier.align(Alignment.TopStart).padding(8.dp))
    }
}

/** Small round type marker (camera / video / mic) over a thumbnail. */
@Composable
fun MomentTypeBadge(type: MomentType, modifier: Modifier = Modifier) {
    val accent = type.accent()
    Row(
        modifier
            .background(Color.Black.copy(alpha = 0.5f), DeepTalkShapes.pill)
            .border(1.dp, accent.copy(alpha = 0.5f), DeepTalkShapes.pill)
            .padding(5.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(type.icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
    }
}

private const val VIDEO_FRAME_MS = 500L
