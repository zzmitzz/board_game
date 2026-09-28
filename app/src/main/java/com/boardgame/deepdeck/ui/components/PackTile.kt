package com.boardgame.deepdeck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.tileImage
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.toComposeColorOrNull

/** Visual variants mapped from section `ui_type`. */
enum class PackTileStyle {
    /** carousel: portrait cover + title below. */
    Carousel,

    /** card_large: wide cover with overlaid title. */
    Large,

    /** list: full-width glass row. */
    Row,

    /** grid: compact row used in 2-row horizontal grids. */
    Compact;

    companion object {
        fun fromUiType(uiType: String?): PackTileStyle = when (uiType?.lowercase()) {
            "grid" -> Compact
            "list" -> Row
            "card_large" -> Large
            else -> Carousel
        }
    }
}

/** Pack accent (admin `accent_color`) or brand. */
@Composable
fun PacksPreview.accent(): Color = accentColor.toComposeColorOrNull() ?: DeepTalkTheme.colors.brand

/**
 * Pack tile: cover, title, keywords, badge, accent glow, press-scale.
 * [sharedKey] enables the shared-element cover transition into Pack detail.
 */
@Composable
fun PackTile(
    pack: PacksPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PackTileStyle = PackTileStyle.Carousel,
    sharedKey: String? = null,
) {
    when (style) {
        PackTileStyle.Carousel -> CarouselTile(pack, onClick, modifier, sharedKey)
        PackTileStyle.Large -> LargeTile(pack, onClick, modifier, sharedKey)
        PackTileStyle.Row -> RowTile(pack, onClick, modifier, sharedKey, thumbSize = 64.dp)
        PackTileStyle.Compact -> RowTile(pack, onClick, modifier, sharedKey, thumbSize = 52.dp, compact = true)
    }
}

/** Cover image with accent-gradient placeholder/fallback. */
@Composable
fun PackCover(
    imageUrl: String?,
    title: String?,
    accent: Color,
    modifier: Modifier = Modifier,
    shape: Shape = DeepTalkShapes.md,
    sharedKey: String? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .sharedPackCover(sharedKey, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(listOf(accent.copy(alpha = 0.85f), DeepTalkTheme.colors.surfaceHigh))
            )
    ) {
        val placeholder: @Composable () -> Unit = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Style,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.55f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (imageUrl.isNullOrBlank()) {
            placeholder()
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Box(Modifier.fillMaxSize().shimmer()) },
                error = { placeholder() }
            )
        }
        overlay()
    }
}

@Composable
private fun CarouselTile(pack: PacksPreview, onClick: () -> Unit, modifier: Modifier, sharedKey: String?) {
    val accent = pack.accent()
    val colors = DeepTalkTheme.colors
    Column(
        modifier = modifier
            .width(148.dp)
            .pressable(onClick = onClick, onClickLabel = pack.title)
    ) {
        PackCover(
            imageUrl = pack.tileImage,
            title = pack.title,
            accent = accent,
            sharedKey = sharedKey,
            shape = DeepTalkShapes.lg,
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
                .glow(accent, radius = 18.dp, alpha = 0.22f)
        ) {
            TileBadges(pack)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = pack.title.orEmpty(),
            style = DeepTalkTheme.type.title.copy(fontSize = DeepTalkTheme.type.label.fontSize * 1.15f),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        pack.keywordsSummarise?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(2.dp))
            Text(
                text = it,
                style = DeepTalkTheme.type.label,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LargeTile(pack: PacksPreview, onClick: () -> Unit, modifier: Modifier, sharedKey: String?) {
    val accent = pack.accent()
    Box(
        modifier = modifier
            .width(288.dp)
            .height(196.dp)
            .glow(accent, radius = 24.dp, alpha = 0.25f)
            .pressable(onClick = onClick, onClickLabel = pack.title)
    ) {
        PackCover(
            imageUrl = pack.tileImage,
            title = pack.title,
            accent = accent,
            sharedKey = sharedKey,
            shape = DeepTalkShapes.lg,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.35f to Color.Transparent,
                            1f to DeepTalkTheme.colors.bgBase.copy(alpha = 0.92f)
                        )
                    )
            )
            TileBadges(pack)
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = pack.title.orEmpty(),
                    style = DeepTalkTheme.type.displaySmall,
                    color = DeepTalkTheme.colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    pack.keywordsSummarise?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = DeepTalkTheme.type.label,
                            color = DeepTalkTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    if (pack.heatLevel != null) HeatDot(pack.heatLevel)
                }
            }
        }
    }
}

@Composable
private fun RowTile(
    pack: PacksPreview,
    onClick: () -> Unit,
    modifier: Modifier,
    sharedKey: String?,
    thumbSize: Dp,
    compact: Boolean = false,
) {
    val accent = pack.accent()
    val colors = DeepTalkTheme.colors
    Row(
        modifier = modifier
            .then(if (compact) Modifier.width(264.dp) else Modifier.fillMaxWidth())
            .clip(DeepTalkShapes.md)
            .background(colors.glass)
            .border(1.dp, colors.outline, DeepTalkShapes.md)
            .pressable(pressedScale = 0.98f, onClick = onClick, onClickLabel = pack.title)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PackCover(
            imageUrl = pack.tileImage,
            title = pack.title,
            accent = accent,
            sharedKey = sharedKey,
            shape = DeepTalkShapes.sm,
            modifier = Modifier.size(thumbSize)
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pack.title.orEmpty(),
                    style = DeepTalkTheme.type.title.copy(fontSize = DeepTalkTheme.type.body.fontSize),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                PackBadge.from(pack.badge)?.let {
                    Spacer(Modifier.width(6.dp))
                    PackBadgeChip(it)
                }
            }
            val meta = pack.keywordsSummarise?.takeIf { it.isNotBlank() }
            if (meta != null) {
                Text(
                    text = meta,
                    style = DeepTalkTheme.type.label,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!compact) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (pack.heatLevel != null) {
                        HeatDot(pack.heatLevel)
                        Spacer(Modifier.width(10.dp))
                    }
                    pack.totalCards?.let {
                        Text(
                            text = pluralStringResource(R.plurals.cards_count, it, it),
                            style = DeepTalkTheme.type.label,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
        if (pack.isPremium == true) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = colors.brand, modifier = Modifier.size(18.dp))
        } else if (!compact) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.TileBadges(pack: PacksPreview) {
    val badge = PackBadge.from(pack.badge)
    if (badge != null) {
        PackBadgeChip(
            badge = badge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )
    }
    if (pack.isPremium == true && badge != PackBadge.PREMIUM) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(26.dp)
                .background(DeepTalkTheme.colors.bgBase.copy(alpha = 0.7f), DeepTalkShapes.pill),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = DeepTalkTheme.colors.brand, modifier = Modifier.size(14.dp))
        }
    }
}
