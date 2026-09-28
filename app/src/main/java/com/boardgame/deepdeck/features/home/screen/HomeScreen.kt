package com.boardgame.deepdeck.features.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.features.home.components.HomeHeader
import com.boardgame.deepdeck.features.home.components.MakeDeckCard
import com.boardgame.deepdeck.features.home.components.QuickPlayStrip
import com.boardgame.deepdeck.features.home.components.SectionBlock
import com.boardgame.deepdeck.features.home.components.SectionSkeleton
import com.boardgame.deepdeck.features.home.components.TonightCard
import com.boardgame.deepdeck.features.home.components.TonightCardSkeleton
import com.boardgame.deepdeck.features.home.components.VibeGrid
import com.boardgame.deepdeck.features.home.components.VibeGridSkeleton
import com.boardgame.deepdeck.features.home.model.VibeUi
import com.boardgame.deepdeck.ui.app.LocalBottomBarPadding
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.ErrorState
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.shareImage

/**
 * Play tab. One primary decision: "Who are you with?" (plan §3.2).
 */
@Composable
fun HomeScreen(
    viewModel: HomeScreenVM,
    onPackClick: (pack: PacksPreview, coverKey: String?) -> Unit,
    onSearchClick: () -> Unit,
    onSeeAllClick: (sectionId: String, title: String?) -> Unit,
    onVibeClick: (VibeUi) -> Unit,
    onStreakClick: () -> Unit,
    onPlayPack: (packId: String) -> Unit,
    onQuickPlay: (packId: String, isCustomPack: Boolean) -> Unit,
    onMakeDeckClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val haptics = LocalDeepTalkHaptics.current
    val shareTitle = stringResource(R.string.share)
    val shareFooter = stringResource(R.string.share_daily_footer)

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeScreenUIEffect.ShowMessage -> snackbar.showSnackbar(context.getString(effect.message))
                is HomeScreenUIEffect.DailyRevealed -> haptics.confirm()
                is HomeScreenUIEffect.ShareDaily ->
                    context.shareImage(effect.uri, chooserTitle = shareTitle, fallbackText = effect.text)
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onPackClick = onPackClick,
        onSearchClick = onSearchClick,
        onSeeAllClick = onSeeAllClick,
        onVibeClick = onVibeClick,
        onStreakClick = onStreakClick,
        onRevealDaily = {
            haptics.medium()
            viewModel.revealDaily()
        },
        onToggleSaveDaily = viewModel::toggleSaveDaily,
        onShareDaily = {
            uiState.dailyCard?.let { card ->
                viewModel.shareDaily(
                    overline = context.getString(R.string.tonight_card_title),
                    fallbackText = "“${card.text}”\n\n$shareFooter"
                )
            }
        },
        onPlayPack = onPlayPack,
        onQuickPlay = {
            viewModel.prepareQuickPlay { onQuickPlay(it.packId, it.isCustomPack) }
        },
        onMakeDeckClick = onMakeDeckClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreenContent(
    uiState: HomeScreenUIState,
    onRefresh: () -> Unit = {},
    onPackClick: (PacksPreview, String?) -> Unit = { _, _ -> },
    onSearchClick: () -> Unit = {},
    onSeeAllClick: (String, String?) -> Unit = { _, _ -> },
    onVibeClick: (VibeUi) -> Unit = {},
    onStreakClick: () -> Unit = {},
    onRevealDaily: () -> Unit = {},
    onToggleSaveDaily: () -> Unit = {},
    onShareDaily: () -> Unit = {},
    onPlayPack: (String) -> Unit = {},
    onQuickPlay: () -> Unit = {},
    onMakeDeckClick: () -> Unit = {},
) {
    val colors = DeepTalkTheme.colors
    val bottomPadding = LocalBottomBarPadding.current
    val tracker = rememberEntranceTracker()
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()

    GlowBackground {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = uiState.isRefreshing,
                    containerColor = colors.surfaceHigh,
                    color = colors.brand,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                )
            }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomPadding + Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxl)
            ) {
                item(key = "header") {
                    HomeHeader(
                        streak = uiState.streak.current,
                        onStreakClick = onStreakClick,
                        onSearchClick = onSearchClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(top = Spacing.md)
                            .staggeredEntrance(0, tracker, "header")
                    )
                }

                when {
                    uiState.isLoading -> {
                        item(key = "skeleton-daily") {
                            TonightCardSkeleton(Modifier.padding(horizontal = Spacing.gutter))
                        }
                        item(key = "skeleton-vibes") { VibeGridSkeleton() }
                        item(key = "skeleton-section") { SectionSkeleton() }
                    }

                    uiState.errorMessage != null && !uiState.hasContent -> {
                        item(key = "error") {
                            ErrorState(
                                modifier = Modifier.padding(top = Spacing.xxl),
                                message = stringResource(R.string.home_error_message),
                                onRetry = onRefresh
                            )
                        }
                    }

                    else -> {
                        uiState.dailyCard?.let { card ->
                            item(key = "daily") {
                                TonightCard(
                                    card = card,
                                    revealed = uiState.dailyRevealed,
                                    saved = uiState.dailySaved,
                                    onReveal = onRevealDaily,
                                    onToggleSave = onToggleSaveDaily,
                                    onShare = onShareDaily,
                                    onPlayPack = card.packId?.let { id -> { onPlayPack(id) } },
                                    modifier = Modifier
                                        .padding(horizontal = Spacing.gutter)
                                        .staggeredEntrance(1, tracker, "daily")
                                )
                            }
                        }
                        if (uiState.vibes.isNotEmpty()) {
                            item(key = "vibes") {
                                VibeGrid(
                                    vibes = uiState.vibes,
                                    onVibeClick = onVibeClick,
                                    modifier = Modifier.staggeredEntrance(2, tracker, "vibes")
                                )
                            }
                        }
                        uiState.quickPlay?.let { quickPlay ->
                            item(key = "quickplay") {
                                QuickPlayStrip(
                                    quickPlay = quickPlay,
                                    onClick = onQuickPlay,
                                    modifier = Modifier.staggeredEntrance(3, tracker, "quickplay")
                                )
                            }
                        }
                        items(uiState.sections, key = { "section-${it.id}" }) { section ->
                            SectionBlock(
                                section = section,
                                onPackClick = onPackClick,
                                onSeeAllClick = onSeeAllClick,
                                modifier = Modifier.staggeredEntrance(4, tracker, "section-${section.id}")
                            )
                        }
                        item(key = "make-deck") {
                            MakeDeckCard(onClick = onMakeDeckClick)
                        }
                    }
                }
            }
        }
    }
}
