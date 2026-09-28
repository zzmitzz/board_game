package com.boardgame.deepdeck.features.pagedetail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.ui.components.CardBackFace
import com.boardgame.deepdeck.ui.components.FlipCard
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.QuestionCardFace
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.color
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.model.CardDetail
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion

/** Three glass stat tiles: cards · play time · players. */
@Composable
fun PackStatTiles(
    totalCards: Int?,
    minutes: Int?,
    players: Int?,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatTile(Icons.Rounded.Style, totalCards?.toString() ?: "–", stringResource(R.string.stat_cards), Modifier.weight(1f))
        StatTile(
            Icons.Rounded.Schedule,
            minutes?.let { stringResource(R.string.stat_minutes_value, it) } ?: "–",
            stringResource(R.string.stat_play_time),
            Modifier.weight(1f)
        )
        StatTile(
            Icons.Rounded.Groups,
            players?.let { stringResource(R.string.stat_players_value, it) } ?: "–",
            stringResource(R.string.stat_players),
            Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(icon: ImageVector, value: String, label: String, modifier: Modifier) {
    val colors = DeepTalkTheme.colors
    GlassSurface(modifier = modifier, shape = DeepTalkShapes.md) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = colors.brand, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = DeepTalkTheme.type.headline, color = colors.textPrimary, maxLines = 1)
            Text(label.uppercase(), style = DeepTalkTheme.type.overline, color = colors.textMuted, maxLines = 1)
        }
    }
}

/**
 * Fan of up to 3 sample cards, face-down; tap one to bring it forward and flip it.
 */
@Composable
fun SampleCardFan(
    cards: List<CardDetail>,
    isLoading: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val haptics = LocalDeepTalkHaptics.current
    Column(modifier.fillMaxWidth()) {
        Text(stringResource(R.string.sample_cards), style = DeepTalkTheme.type.headline, color = colors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.sample_cards_hint), style = DeepTalkTheme.type.body, color = colors.textSecondary)
        Spacer(Modifier.height(18.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> SkeletonBox(Modifier.width(200.dp).height(280.dp), DeepTalkShapes.xl)
                cards.isEmpty() -> Text(
                    stringResource(R.string.sample_cards_empty),
                    style = DeepTalkTheme.type.body,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
                else -> {
                    var front by rememberSaveable { mutableIntStateOf(cards.size / 2) }
                    var flipped by remember { mutableStateOf(setOf<Int>()) }
                    val n = cards.size
                    cards.forEachIndexed { index, card ->
                        val slot = index - (n - 1) / 2f
                        val isFront = index == front
                        val rotation by animateFloatAsState(
                            if (isFront) 0f else slot * 9f,
                            Motion.pressSpring(),
                            label = "fanRotation"
                        )
                        val offsetX by animateDpAsState(
                            if (isFront) 0.dp else (slot * 64).dp,
                            Motion.pressSpring(),
                            label = "fanOffset"
                        )
                        val scale by animateFloatAsState(if (isFront) 1f else 0.9f, Motion.pressSpring(), label = "fanScale")
                        val level = CardLevel.from(card.category)
                        FlipCard(
                            flipped = index in flipped,
                            modifier = Modifier
                                .zIndex(if (isFront) 10f else index.toFloat())
                                .offset(x = offsetX)
                                .rotate(rotation)
                                .width(200.dp * scale)
                                .height(280.dp * scale)
                                .glow(if (index in flipped) level.color else accent, radius = 20.dp, alpha = if (isFront) 0.25f else 0.1f)
                                .pressable(pressedScale = 0.97f, haptic = false) {
                                    haptics.press()
                                    if (!isFront) {
                                        front = index
                                    } else {
                                        flipped = if (index in flipped) flipped - index else flipped + index
                                    }
                                },
                            front = {
                                CardBackFace(
                                    accent = accent,
                                    subtitle = if (isFront) stringResource(R.string.tap_to_flip) else null
                                )
                            },
                            back = {
                                QuestionCardFace(
                                    text = card.description,
                                    level = level,
                                    textStyle = DeepTalkTheme.type.displaySmall.copy(
                                        fontSize = DeepTalkTheme.type.title.fontSize
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Expandable "How to play". Hidden entirely by callers when the text is blank. */
@Composable
fun HowToPlayCard(instruction: String, modifier: Modifier = Modifier) {
    val colors = DeepTalkTheme.colors
    var expanded by rememberSaveable { mutableStateOf(false) }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, label = "htpArrow")
    GlassSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier
                .pressable(pressedScale = 0.99f) { expanded = !expanded }
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, tint = colors.brand)
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.how_to_play),
                    style = DeepTalkTheme.type.title,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.rotate(arrow)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = instruction,
                    style = DeepTalkTheme.type.body,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
