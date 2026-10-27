package com.boardgame.deepdeck.ui.components

import android.net.Uri
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.boardgame.deepdeck.ui.model.CardAsset
import com.boardgame.deepdeck.ui.model.CardAssetKind
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.utils.openWebPage

private val CardAsset.isVisual: Boolean
    get() = kind == CardAssetKind.IMAGE || kind == CardAssetKind.VIDEO

/** Accessibility text, degrading the same way the admin preview does. */
private val CardAsset.label: String
    get() = title ?: caption ?: altText ?: Uri.parse(url).host?.removePrefix("www.") ?: kind.name

/** Embeds open in the provider's app / page rather than the bare embed player. */
private fun CardAsset.externalUrl(): String = when {
    url.contains("youtube.com/embed/") -> "https://www.youtube.com/watch?v=" + url.substringAfter("/embed/").substringBefore('?')
    url.contains("open.spotify.com/embed/") -> url.replace("open.spotify.com/embed/", "open.spotify.com/")
    else -> url
}

/** The first inline image / video is shown large; everything else becomes a chip. */
fun List<CardAsset>.leadVisual(): CardAsset? = firstOrNull { it.isVisual }

/**
 * Full-bleed background (API role BACKGROUND): image, or a muted looping video,
 * under a scrim so the question stays readable.
 */
@Composable
fun BoxScope.CardMediaBackground(asset: CardAsset) {
    when (asset.kind) {
        CardAssetKind.VIDEO -> CardVideo(asset, background = true, modifier = Modifier.matchParentSize())
        else -> AsyncImage(
            model = asset.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
    }
    Box(
        Modifier
            .matchParentSize()
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.7f))))
    )
}

/** Inline image or video (tap to play) letterboxed in a rounded frame. */
@Composable
fun CardInlineVisual(asset: CardAsset, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(DeepTalkShapes.md)
            .background(Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        if (asset.kind == CardAssetKind.VIDEO) {
            CardVideo(asset, background = false, modifier = Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = asset.url,
                contentDescription = asset.altText ?: asset.caption,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** Audio, links, embeds and any extra images / videos as a scrollable chip row. */
@Composable
fun CardMediaChips(assets: List<CardAsset>, modifier: Modifier = Modifier) {
    if (assets.isEmpty()) return
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        assets.forEach { asset ->
            if (asset.kind == CardAssetKind.AUDIO) AudioChip(asset) else LinkChip(asset)
        }
    }
}

@Composable
private fun MediaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Row(
        Modifier
            .clip(DeepTalkShapes.pill)
            .background(Color.White.copy(alpha = 0.12f))
            .pressable(onClick = onClick, onClickLabel = text)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
        Text(
            text,
            style = DeepTalkTheme.type.label,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 180.dp)
        )
    }
}

@Composable
private fun LinkChip(asset: CardAsset) {
    val context = LocalContext.current
    val icon = when (asset.kind) {
        CardAssetKind.IMAGE -> Icons.Rounded.Image
        CardAssetKind.VIDEO -> Icons.Rounded.Movie
        else -> Icons.AutoMirrored.Rounded.OpenInNew
    }
    MediaChip(icon, asset.label) { context.openWebPage(asset.externalUrl()) }
}

@Composable
private fun AudioChip(asset: CardAsset) {
    val context = LocalContext.current
    var playing by remember { mutableStateOf(false) }
    // Prepared on first tap so cards never download audio nobody plays.
    val player = remember(asset.url) {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playing = isPlaying
                }
            })
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    LifecycleStartEffect(player) { onStopOrDispose { player.pause() } }
    MediaChip(if (playing) Icons.Rounded.Pause else Icons.Rounded.MusicNote, asset.label) {
        if (player.mediaItemCount == 0) {
            player.setMediaItem(MediaItem.fromUri(asset.url))
            player.prepare()
        }
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        if (player.isPlaying) player.pause() else player.play()
    }
}

/**
 * ExoPlayer on a TextureView (a SurfaceView would ignore the card's flip / swipe transforms).
 * Background: muted, looping, autoplay, cropped. Inline: tap to play / pause with sound, fitted.
 */
@Composable
private fun CardVideo(asset: CardAsset, background: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    var videoSize by remember { mutableStateOf(VideoSize.UNKNOWN) }
    var playing by remember { mutableStateOf(false) }
    var firstFrame by remember { mutableStateOf(false) }
    val player = remember(asset.url) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            if (background) volume = 0f
            addListener(object : Player.Listener {
                override fun onVideoSizeChanged(size: VideoSize) {
                    videoSize = size
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playing = isPlaying
                }

                override fun onRenderedFirstFrame() {
                    firstFrame = true
                }
            })
            setMediaItem(MediaItem.fromUri(asset.url))
            prepare()
            playWhenReady = background
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    LifecycleStartEffect(player) {
        if (background) player.play()
        onStopOrDispose { player.pause() }
    }

    BoxWithConstraints(
        modifier
            .clipToBounds()
            .then(
                if (background) Modifier
                else Modifier.pressable(pressedScale = 1f, onClickLabel = asset.label) {
                    if (player.isPlaying) player.pause() else player.play()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val boxW = constraints.maxWidth.toFloat()
        val boxH = constraints.maxHeight.toFloat()
        val videoW = videoSize.width * videoSize.pixelWidthHeightRatio
        val videoH = videoSize.height.toFloat()
        // TextureView stretches to its bounds, so size it to the video's aspect ratio ourselves.
        val viewModifier = if (videoW > 0f && videoH > 0f && boxW > 0f && boxH > 0f) {
            val scale = if (background) maxOf(boxW / videoW, boxH / videoH) else minOf(boxW / videoW, boxH / videoH)
            with(density) { Modifier.requiredSize((videoW * scale).toDp(), (videoH * scale).toDp()) }
        } else {
            Modifier.fillMaxSize()
        }
        AndroidView(
            factory = { TextureView(it).also(player::setVideoTextureView) },
            modifier = viewModifier
        )
        if (!firstFrame && asset.thumbnailUrl != null) {
            AsyncImage(
                model = asset.thumbnailUrl,
                contentDescription = null,
                contentScale = if (background) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier.matchParentSize()
            )
        }
        if (!background && !playing) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}
