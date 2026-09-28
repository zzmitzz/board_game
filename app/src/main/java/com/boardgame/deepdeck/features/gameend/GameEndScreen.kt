package com.boardgame.deepdeck.features.gameend

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.features.ingame.InGameUiEffect
import com.boardgame.deepdeck.features.ingame.InGameVM
import com.boardgame.deepdeck.features.ingame.engine.GameSummary
import com.boardgame.deepdeck.features.ingame.engine.LeaderboardEntry
import com.boardgame.deepdeck.features.ingame.engine.PlayerAward
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.EmptyState
import com.boardgame.deepdeck.ui.components.GhostButton
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.ParticleBurst
import com.boardgame.deepdeck.ui.components.PlayerAvatar
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.QuestionCardFace
import com.boardgame.deepdeck.ui.components.SectionHeader
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.shareImage
import kotlinx.coroutines.flow.flowOf

/**
 * End of game — "Wrapped" (plan §3.3 / §7.2.8): winner reveal with confetti + win haptic,
 * computed awards, leaderboard, card of the night, share recap image, play again / home.
 * Never crashes when the session is gone (process death): shows an empty state instead.
 */
@Composable
fun GameEndScreen(
    vm: InGameVM,
    onHomeClick: () -> Unit,
    onPlayAgainClick: () -> Unit,
) {
    val summary by vm.summary.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current

    LaunchedEffect(vm) {
        vm.uiEffect.collect { effect ->
            when (effect) {
                is InGameUiEffect.Share -> context.shareImage(
                    effect.uri,
                    text = effect.fallbackText,
                    chooserTitle = context.getString(R.string.share),
                    fallbackText = effect.fallbackText
                )
                is InGameUiEffect.ShowMessage -> snackbar.showSnackbar(context.getString(effect.message))
                else -> Unit
            }
        }
    }
    BackHandler { onHomeClick() }

    val current = summary
    if (current == null || current.leaderboard.isEmpty()) {
        GlowBackground {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyState(
                    title = stringResource(R.string.results_unavailable_title),
                    message = stringResource(R.string.results_unavailable_message),
                    icon = Icons.Rounded.EmojiEvents
                )
                Spacer(Modifier.height(Spacing.md))
                PrimaryButton(stringResource(R.string.home), onHomeClick, leadingIcon = Icons.Rounded.Home)
            }
        }
        return
    }

    val cardSaved by remember(current.cardOfTheNight?.id) {
        current.cardOfTheNight?.let { vm.isCardSaved(it.id) } ?: flowOf(false)
    }.collectAsState(initial = false)

    GameEndContent(
        summary = current,
        cardOfTheNightSaved = cardSaved,
        onHome = onHomeClick,
        onPlayAgain = onPlayAgainClick,
        onShareRecap = vm::shareRecap,
        onSaveCardOfTheNight = vm::toggleSaveCardOfTheNight,
        onShareCardOfTheNight = { current.cardOfTheNight?.let { vm.shareCard(it, current.packTitle) } },
    )
}

@Composable
private fun GameEndContent(
    summary: GameSummary,
    cardOfTheNightSaved: Boolean,
    onHome: () -> Unit,
    onPlayAgain: () -> Unit,
    onShareRecap: () -> Unit,
    onSaveCardOfTheNight: () -> Unit,
    onShareCardOfTheNight: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val haptics = LocalDeepTalkHaptics.current
    var celebrated by rememberSaveable { mutableStateOf(false) }
    var confettiTrigger by remember { mutableStateOf<Any?>(null) }
    val tracker = rememberEntranceTracker()

    LaunchedEffect(Unit) {
        if (!celebrated) {
            celebrated = true
            confettiTrigger = System.nanoTime()
            haptics.win()
        }
    }

    GlowBackground(accent = colors.gold, secondaryAccent = colors.brandStrong) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = Spacing.xl)
            ) {
                item(key = "top") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.wrapped_title).uppercase(),
                                style = DeepTalkTheme.type.overline,
                                color = colors.gold
                            )
                            Text(
                                summary.packTitle,
                                style = DeepTalkTheme.type.title,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        GlassIconButton(Icons.Rounded.Close, stringResource(R.string.home), onHome, size = 40.dp)
                    }
                }
                summary.winner?.let { winner ->
                    item(key = "winner") {
                        WinnerHero(winner, confettiTrigger)
                    }
                }
                if (summary.awards.size > 1) {
                    item(key = "awards-header") {
                        SectionHeader(
                            title = stringResource(R.string.awards_title),
                            modifier = Modifier.padding(horizontal = Spacing.gutter, vertical = Spacing.sm)
                        )
                    }
                    item(key = "awards") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Spacing.gutter),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(summary.awards.drop(1), key = { it.type.name }) { award ->
                                AwardCard(award)
                            }
                        }
                    }
                }
                item(key = "leaderboard-header") {
                    SectionHeader(
                        title = stringResource(R.string.leaderboard),
                        modifier = Modifier.padding(start = Spacing.gutter, end = Spacing.gutter, top = Spacing.xl, bottom = Spacing.sm)
                    )
                }
                summary.leaderboard.forEachIndexed { index, entry ->
                    item(key = "rank-${entry.player.id}") {
                        LeaderboardRow(
                            entry = entry,
                            award = summary.awardOf(entry.player),
                            modifier = Modifier
                                .padding(horizontal = Spacing.gutter, vertical = 4.dp)
                                .staggeredEntrance(index, tracker, "rank-${entry.player.id}")
                        )
                    }
                }
                summary.cardOfTheNight?.let { card ->
                    item(key = "cotn") {
                        Column(Modifier.padding(horizontal = Spacing.gutter)) {
                            SectionHeader(
                                title = stringResource(R.string.card_of_the_night),
                                subtitle = stringResource(
                                    if (summary.cardOfTheNightLoved) R.string.card_of_the_night_loved
                                    else R.string.card_of_the_night_longest
                                ),
                                modifier = Modifier.padding(top = Spacing.xl, bottom = Spacing.sm)
                            )
                            val level = CardLevel.from(card.category)
                            QuestionCardFace(
                                text = card.description,
                                level = level,
                                textStyle = DeepTalkTheme.type.displaySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                footer = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlassIconButton(
                                            icon = if (cardOfTheNightSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                            contentDescription = stringResource(R.string.save_card_action),
                                            onClick = { if (!cardOfTheNightSaved) onSaveCardOfTheNight() },
                                            size = 40.dp,
                                            tint = if (cardOfTheNightSaved) colors.rose else colors.textPrimary,
                                            active = cardOfTheNightSaved
                                        )
                                        GlassIconButton(
                                            icon = Icons.Rounded.IosShare,
                                            contentDescription = stringResource(R.string.share_card_image),
                                            onClick = onShareCardOfTheNight,
                                            size = 40.dp
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                item(key = "stats") {
                    val minutes = (summary.durationSeconds / 60).coerceAtLeast(1)
                    Text(
                        stringResource(
                            R.string.recap_stats_line,
                            summary.totalCompleted + summary.totalForfeited,
                            minutes
                        ),
                        style = DeepTalkTheme.type.label,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.xl)
                    )
                }
            }

            // Sticky actions
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.bgBase.copy(alpha = 0.85f))
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.share_recap),
                    onClick = onShareRecap,
                    leadingIcon = Icons.Rounded.IosShare,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    GhostButton(
                        text = stringResource(R.string.play_again),
                        onClick = onPlayAgain,
                        leadingIcon = Icons.Rounded.Replay,
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = stringResource(R.string.home),
                        onClick = onHome,
                        leadingIcon = Icons.Rounded.Home,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        // Confetti over everything
        ParticleBurst(
            trigger = confettiTrigger,
            modifier = Modifier.fillMaxSize(),
            colors = listOf(colors.brand, colors.mint, colors.rose, colors.gold, colors.sky),
            particleCount = 90,
            durationMs = 2000,
            spread = 1.35f,
            gravity = 1.2f
        )
    }
}

@Composable
private fun WinnerHero(winner: LeaderboardEntry, confettiTrigger: Any?) {
    val colors = DeepTalkTheme.colors
    val reduced = DeepTalkTheme.reducedMotion
    val pulse = if (reduced) 0.35f else rememberInfiniteTransition(label = "winnerGlow").animateFloat(
        initialValue = 0.22f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "winnerGlowAlpha"
    ).value
    val description = stringResource(R.string.winner_announcement, winner.player.name)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.gutter, vertical = Spacing.lg)
            .semantics(mergeDescendants = true) { contentDescription = description; heading() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            ParticleBurst(trigger = confettiTrigger, modifier = Modifier.size(220.dp), particleCount = 36)
            PlayerAvatar(
                winner.player,
                size = 120.dp,
                ring = colors.gold,
                modifier = Modifier.glow(colors.gold, radius = 48.dp, alpha = pulse, offsetY = 0.dp)
            )
            Text(
                "👑",
                fontSize = 40.sp,
                modifier = Modifier.offset(y = (-72).dp)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            "🏆 " + stringResource(R.string.award_winner).uppercase(),
            style = DeepTalkTheme.type.overline,
            color = colors.gold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            winner.player.name,
            style = DeepTalkTheme.type.hero,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.recap_winner_stat, winner.score.getScore(), winner.score.numberCardCompleted),
            style = DeepTalkTheme.type.body,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun AwardCard(award: PlayerAward) {
    val colors = DeepTalkTheme.colors
    GlassSurface(Modifier.widthIn(min = 150.dp, max = 180.dp), tint = colors.gold) {
        Column(Modifier.padding(16.dp)) {
            Text(award.type.emoji, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(award.type.title).uppercase(),
                style = DeepTalkTheme.type.overline,
                color = colors.gold
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(award.player, size = 24.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    award.player.name,
                    style = DeepTalkTheme.type.title,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(award.type.description),
                style = DeepTalkTheme.type.label,
                color = colors.textSecondary,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, award: PlayerAward?, modifier: Modifier) {
    val colors = DeepTalkTheme.colors
    val isWinner = entry.rank == 1
    GlassSurface(modifier.fillMaxWidth(), shape = DeepTalkShapes.md, tint = if (isWinner) colors.gold else null) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#${entry.rank}",
                style = DeepTalkTheme.type.title,
                color = if (isWinner) colors.gold else colors.textMuted,
                modifier = Modifier.width(36.dp)
            )
            PlayerAvatar(entry.player, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.player.name,
                        style = DeepTalkTheme.type.title,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    award?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(it.type.emoji, fontSize = 14.sp)
                    }
                }
                Text(
                    stringResource(
                        R.string.leaderboard_line,
                        entry.score.numberCardCompleted,
                        entry.score.numberCardForfeited
                    ),
                    style = DeepTalkTheme.type.label,
                    color = colors.textSecondary
                )
            }
            Box(
                Modifier
                    .clip(CircleShape)
                    .background((if (isWinner) colors.gold else colors.brand).copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    pluralStringResource(R.plurals.points, entry.score.getScore(), entry.score.getScore()),
                    style = DeepTalkTheme.type.label,
                    color = if (isWinner) colors.gold else colors.brand
                )
            }
        }
    }
}
