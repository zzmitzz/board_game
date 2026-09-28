package com.boardgame.deepdeck.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.home.model.VibeUi
import com.boardgame.deepdeck.features.home.screen.QuickPlayUi
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.SkeletonLine
import com.boardgame.deepdeck.ui.components.StreakChip
import com.boardgame.deepdeck.ui.components.VibeTile
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import java.util.Calendar

/** Time-aware greeting resource. */
fun greetingRes(hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Int = when (hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..16 -> R.string.greeting_afternoon
    in 17..21 -> R.string.greeting_evening
    else -> R.string.greeting_night
}

/** Header row: greeting · 🔥 streak chip · search icon (no fake text field). */
@Composable
fun HomeHeader(
    streak: Int,
    onStreakClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val greeting = remember { greetingRes() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = DeepTalkTheme.type.overline,
                color = colors.brand
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(greeting),
                style = DeepTalkTheme.type.hero.copy(fontSize = DeepTalkTheme.type.headline.fontSize * 1.18f),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StreakChip(count = streak, onClick = onStreakClick)
        Spacer(Modifier.width(10.dp))
        GlassIconButton(
            icon = Icons.Rounded.Search,
            contentDescription = stringResource(R.string.search),
            onClick = onSearchClick,
            size = 40.dp
        )
    }
}

/** "Who are you with?" — 2-column grid of large gradient vibe tiles. */
@Composable
fun VibeGrid(
    vibes: List<VibeUi>,
    onVibeClick: (VibeUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = Spacing.gutter)) {
        Text(
            text = stringResource(R.string.who_are_you_with),
            style = DeepTalkTheme.type.headline,
            color = DeepTalkTheme.colors.textPrimary
        )
        Spacer(Modifier.height(Spacing.md))
        vibes.chunked(2).forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { colIndex, vibe ->
                    VibeTile(
                        name = vibe.name,
                        iconKey = vibe.iconKey,
                        colorStart = vibe.colorStart,
                        colorEnd = vibe.colorEnd,
                        subtitle = vibe.packCount?.let { pluralStringResource(R.plurals.packs_count, it, it) },
                        onClick = { onVibeClick(vibe) },
                        modifier = Modifier
                            .weight(1f)
                            .staggeredEntrance(rowIndex * 2 + colIndex, key = "vibe-${vibe.id}")
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun VibeGridSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = Spacing.gutter)) {
        SkeletonLine(width = 200.dp, height = 22.dp)
        Spacer(Modifier.height(Spacing.md))
        repeat(2) { r ->
            if (r > 0) Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(2) {
                    SkeletonBox(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1.18f),
                        DeepTalkShapes.lg
                    )
                }
            }
        }
    }
}

/** Continue / Quick Play strip: one tap → setup pre-filled with the last roster. */
@Composable
fun QuickPlayStrip(
    quickPlay: QuickPlayUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val names = quickPlay.players.map { it.name }
    val shown = names.take(2).joinToString(", ")
    val extra = names.size - 2
    val who = if (extra > 0) stringResource(R.string.quick_play_players_more, shown, extra) else shown
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter)
            .pressable(pressedScale = 0.98f, onClick = onClick),
        tint = colors.brand
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar stack
            Box(Modifier.width((28 + 18 * (quickPlay.players.take(3).size - 1)).dp).height(32.dp)) {
                quickPlay.players.take(3).forEachIndexed { i, player ->
                    Box(
                        modifier = Modifier
                            .offset(x = (18 * i).dp)
                            .size(32.dp)
                            .border(2.dp, colors.bgElevated, CircleShape)
                            .clip(CircleShape)
                            .background(player.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.name.take(1).uppercase(),
                            style = DeepTalkTheme.type.label.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quick_play_title, who),
                    style = DeepTalkTheme.type.title.copy(fontSize = DeepTalkTheme.type.body.fontSize),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = quickPlay.packName,
                    style = DeepTalkTheme.type.label,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .glow(colors.brand, radius = 14.dp, alpha = 0.35f, offsetY = 0.dp)
                    .clip(CircleShape)
                    .background(colors.brandGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.play), tint = colors.brandOn)
            }
        }
    }
}

/** Footer: "Make your own deck" → Library › Custom packs. */
@Composable
fun MakeDeckCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter)
            .clip(DeepTalkShapes.lg)
            .background(
                Brush.linearGradient(listOf(colors.surfaceHigh, colors.surface))
            )
            .border(1.dp, colors.outline, DeepTalkShapes.lg)
            .pressable(pressedScale = 0.98f, onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.make_your_own_deck),
                style = DeepTalkTheme.type.displaySmall,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.make_your_own_deck_desc),
                style = DeepTalkTheme.type.body,
                color = colors.textSecondary
            )
        }
        Spacer(Modifier.width(12.dp))
        Icon(
            Icons.Rounded.AddCircleOutline,
            contentDescription = null,
            tint = colors.brand,
            modifier = Modifier.size(36.dp)
        )
    }
}
