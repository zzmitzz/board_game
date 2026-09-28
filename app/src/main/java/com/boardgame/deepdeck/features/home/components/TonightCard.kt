package com.boardgame.deepdeck.features.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.home.screen.DailyCardUi
import com.boardgame.deepdeck.ui.components.CardBackFace
import com.boardgame.deepdeck.ui.components.FlipCard
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.ParticleBurst
import com.boardgame.deepdeck.ui.components.QuestionCardFace
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.color
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.sheenSweep
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme

/**
 * Tonight's Card hero (~260dp): face-down with a shimmer sweep every 3s;
 * tap flips it (+ gold particles). Actions: ♥ Save · Share · Play this pack.
 */
@Composable
fun TonightCard(
    card: DailyCardUi,
    revealed: Boolean,
    saved: Boolean,
    onReveal: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onPlayPack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val glowColor = if (revealed) card.level.color else colors.brand
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            FlipCard(
                flipped = revealed,
                modifier = Modifier
                    .fillMaxSize()
                    .glow(glowColor, radius = 36.dp, alpha = 0.28f, offsetY = 14.dp)
                    .pressable(
                        enabled = !revealed,
                        pressedScale = 0.97f,
                        onClickLabel = stringResource(R.string.tonight_tap_to_reveal),
                        onClick = onReveal
                    ),
                front = {
                    CardBackFace(
                        modifier = Modifier
                            .fillMaxSize()
                            .sheenSweep(),
                        title = stringResource(R.string.tonight_card_title),
                        subtitle = stringResource(R.string.tonight_tap_to_reveal)
                    )
                },
                back = {
                    QuestionCardFace(
                        text = card.text,
                        level = card.level,
                        textStyle = DeepTalkTheme.type.displaySmall.copy(
                            fontSize = if (card.text.length > 110) DeepTalkTheme.type.body.fontSize * 1.25f
                            else DeepTalkTheme.type.displaySmall.fontSize
                        ),
                        overline = stringResource(R.string.tonight_card_title),
                        modifier = Modifier.fillMaxSize(),
                        footer = card.packTitle?.let { title ->
                            {
                                Text(
                                    text = title,
                                    style = DeepTalkTheme.type.label,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    )
                }
            )
            ParticleBurst(
                trigger = if (revealed) card.cardId else null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(14.dp))
        AnimatedContent(
            targetState = revealed,
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut()
            },
            label = "tonightActions"
        ) { isRevealed ->
            if (isRevealed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GhostButton(
                        text = stringResource(if (saved) R.string.saved else R.string.save_card_action),
                        leadingIcon = if (saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentColor = if (saved) colors.rose else colors.textPrimary,
                        onClick = onToggleSave
                    )
                    GhostButton(
                        text = stringResource(R.string.share),
                        leadingIcon = Icons.Rounded.IosShare,
                        onClick = onShare
                    )
                    Spacer(Modifier.weight(1f))
                    if (onPlayPack != null) {
                        Row(
                            modifier = Modifier
                                .pressable(onClick = onPlayPack)
                                .height(48.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.play_this_pack_short),
                                style = DeepTalkTheme.type.label,
                                color = colors.brand
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = colors.brand,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.TouchApp, null, tint = colors.textMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.tonight_reveal_hint),
                        style = DeepTalkTheme.type.label,
                        color = colors.textMuted
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Rounded.LocalFireDepartment,
                        null,
                        tint = colors.ember,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TonightCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        SkeletonBox(
            Modifier
                .fillMaxWidth()
                .height(260.dp),
            DeepTalkShapes.xl
        )
        Spacer(Modifier.height(62.dp))
    }
}
