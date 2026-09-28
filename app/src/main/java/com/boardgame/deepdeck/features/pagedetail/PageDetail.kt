package com.boardgame.deepdeck.features.pagedetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.home.HomeRoute
import com.boardgame.deepdeck.features.pagedetail.components.HowToPlayCard
import com.boardgame.deepdeck.features.pagedetail.components.PackStatTiles
import com.boardgame.deepdeck.features.pagedetail.components.SampleCardFan
import com.boardgame.deepdeck.ui.components.ErrorState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.HeatMeter
import com.boardgame.deepdeck.ui.components.OverlinePill
import com.boardgame.deepdeck.ui.components.PackBadge
import com.boardgame.deepdeck.ui.components.PackBadgeChip
import com.boardgame.deepdeck.ui.components.PackCover
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.SkeletonLine
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.ui.theme.toComposeColorOrNull

private const val CTA_DEBOUNCE_MS = 1_000L

@Composable
fun PageDetailScreen(
    route: HomeRoute.PackDetail,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    viewModel: PageDetailVM = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastClick by remember { mutableLongStateOf(0L) }
    PageDetailContent(
        route = route,
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        onPlayClick = {
            val now = System.currentTimeMillis()
            if (now - lastClick >= CTA_DEBOUNCE_MS) {
                lastClick = now
                onPlayClick()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageDetailContent(
    route: HomeRoute.PackDetail,
    uiState: PageDetailUIState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onPlayClick: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val pack = uiState.pack
    val accent = (pack?.accentColor ?: route.accent).toComposeColorOrNull() ?: colors.brand
    val title = pack?.titleCard?.takeIf { it.isNotBlank() } ?: route.title.orEmpty()
    val cover = route.coverUrl ?: pack?.coverImageUrl ?: pack?.thumb
    val isPremium = pack?.isPremium == true
    var showPremiumSheet by remember { mutableStateOf(false) }

    GlowBackground(accent = accent) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 132.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item(key = "hero") {
                Box(Modifier.fillMaxWidth()) {
                    PackCover(
                        imageUrl = cover,
                        title = title,
                        accent = accent,
                        sharedKey = route.coverKey,
                        shape = DeepTalkShapes.xl.copy(
                            topStart = androidx.compose.foundation.shape.CornerSize(0.dp),
                            topEnd = androidx.compose.foundation.shape.CornerSize(0.dp)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        0f to colors.bgBase.copy(alpha = 0.35f),
                                        0.35f to Color.Transparent,
                                        1f to colors.bgBase
                                    )
                                )
                        )
                    }
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = Spacing.gutter)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PackBadge.from(pack?.badge)?.let { PackBadgeChip(it, solid = true) }
                            pack?.tag?.takeIf { it.isNotBlank() }?.let { OverlinePill(it, accent) }
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Text(title, style = DeepTalkTheme.type.hero, color = colors.textPrimary)
                    }
                }
            }

            item(key = "description") {
                Column(Modifier.padding(horizontal = Spacing.gutter)) {
                    when {
                        uiState.isLoading && pack == null -> {
                            SkeletonLine(width = 280.dp)
                            Spacer(Modifier.height(8.dp))
                            SkeletonLine(width = 220.dp)
                        }
                        !pack?.description.isNullOrBlank() -> Text(
                            text = pack.description,
                            style = DeepTalkTheme.type.body,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            if (uiState.errorMessage != null && pack == null) {
                item(key = "error") {
                    ErrorState(
                        message = stringResource(R.string.pack_error_message),
                        onRetry = onRetry
                    )
                }
            }

            if (pack != null) {
                item(key = "stats") {
                    PackStatTiles(
                        totalCards = pack.totalCards,
                        minutes = pack.estimateTimePlay,
                        players = pack.suggestNumberPlayers,
                        modifier = Modifier.padding(horizontal = Spacing.gutter)
                    )
                }
                item(key = "heat") {
                    GlassSurface(Modifier.padding(horizontal = Spacing.gutter)) {
                        HeatMeter(heat = pack.heatLevel, modifier = Modifier.padding(18.dp))
                    }
                }
            }

            item(key = "samples") {
                SampleCardFan(
                    cards = uiState.packSampleCard,
                    isLoading = uiState.isLoadingSampleCard,
                    accent = accent,
                    modifier = Modifier.padding(horizontal = Spacing.gutter)
                )
            }

            pack?.howToPlay?.takeIf { it.isNotBlank() }?.let { instruction ->
                item(key = "howto") {
                    HowToPlayCard(instruction, Modifier.padding(horizontal = Spacing.gutter))
                }
            }
        }

        // Top bar
        GlassIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.back),
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = Spacing.md, top = Spacing.xs)
        )

        // Sticky CTA
        AnimatedVisibility(
            visible = pack != null,
            enter = fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, colors.bgBase, colors.bgBase)))
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.gutter)
                    .padding(top = Spacing.xxl, bottom = Spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = stringResource(if (isPremium) R.string.premium else R.string.free),
                            style = DeepTalkTheme.type.headline,
                            color = if (isPremium) colors.gold else colors.textPrimary
                        )
                        Text(
                            text = stringResource(if (isPremium) R.string.premium_subtitle else R.string.free_subtitle),
                            style = DeepTalkTheme.type.label,
                            color = colors.textMuted
                        )
                    }
                    Spacer(Modifier.width(Spacing.md))
                    PrimaryButton(
                        text = stringResource(if (isPremium) R.string.unlock_pack else R.string.play_game),
                        leadingIcon = if (isPremium) Icons.Rounded.Lock else Icons.Rounded.PlayArrow,
                        onClick = { if (isPremium) showPremiumSheet = true else onPlayClick() },
                        glowColor = accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showPremiumSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPremiumSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.bgElevated,
            contentColor = colors.textPrimary,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(72.dp)
                        .glow(colors.gold, radius = 28.dp, alpha = 0.35f, offsetY = 0.dp)
                        .background(colors.gold.copy(alpha = 0.16f), CircleShape)
                        .border(1.dp, colors.gold.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Diamond, contentDescription = null, tint = colors.gold, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(Spacing.md))
                Text(
                    stringResource(R.string.premium_coming_soon_title),
                    style = DeepTalkTheme.type.display,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    stringResource(R.string.premium_coming_soon_message),
                    style = DeepTalkTheme.type.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Spacing.xl))
                PrimaryButton(
                    text = stringResource(R.string.got_it),
                    onClick = { showPremiumSheet = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
