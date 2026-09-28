package com.boardgame.deepdeck.features.home.vibe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.features.home.HomeRoute
import com.boardgame.deepdeck.ui.components.EmptyState
import com.boardgame.deepdeck.ui.components.ErrorState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.PackTile
import com.boardgame.deepdeck.ui.components.PackTileStyle
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.packCoverKey
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.components.vibeIconPainter
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing

/** Packs for one vibe, as large tiles sorted by heat. */
@Composable
fun VibeScreen(
    route: HomeRoute.Vibe,
    onBackClick: () -> Unit,
    onPackClick: (pack: PacksPreview, coverKey: String?) -> Unit,
    viewModel: VibeVM = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    VibeScreenContent(
        route = route,
        uiState = uiState,
        onBackClick = onBackClick,
        onPackClick = onPackClick,
        onRetry = viewModel::load
    )
}

@Composable
private fun VibeScreenContent(
    route: HomeRoute.Vibe,
    uiState: VibeUiState,
    onBackClick: () -> Unit,
    onPackClick: (PacksPreview, String?) -> Unit,
    onRetry: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val start = Color(route.colorStart)
    val end = Color(route.colorEnd)
    val tracker = rememberEntranceTracker()
    val source = "vibe-${route.vibeId}"

    GlowBackground(accent = start, secondaryAccent = end) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "top") {
                Column(
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = Spacing.gutter)
                        .padding(top = Spacing.xs, bottom = Spacing.md)
                ) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        onClick = onBackClick
                    )
                    Spacer(Modifier.height(Spacing.xl))
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .glow(end, radius = 24.dp, alpha = 0.4f, offsetY = 4.dp)
                            .background(Brush.linearGradient(listOf(start, end)), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = vibeIconPainter(route.iconKey),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Text(route.name, style = DeepTalkTheme.type.hero, color = colors.textPrimary)
                    val subtitle = route.description
                        ?: (uiState as? VibeUiState.Success)?.packs?.size?.let {
                            pluralStringResource(R.plurals.packs_count, it, it)
                        }
                    if (subtitle != null) {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(subtitle, style = DeepTalkTheme.type.body, color = colors.textSecondary)
                    }
                }
            }

            when (uiState) {
                VibeUiState.Loading -> items(3) {
                    SkeletonBox(
                        Modifier
                            .padding(horizontal = Spacing.gutter)
                            .fillMaxWidth()
                            .height(196.dp),
                        DeepTalkShapes.lg
                    )
                }

                is VibeUiState.Error -> item(key = "error") { ErrorState(onRetry = onRetry) }

                is VibeUiState.Success -> {
                    if (uiState.packs.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                title = stringResource(R.string.no_packs_found),
                                message = stringResource(R.string.vibe_empty_message),
                                icon = Icons.Rounded.Inbox
                            )
                        }
                    } else {
                        itemsIndexed(uiState.packs, key = { _, p -> p.id.orEmpty() }) { index, pack ->
                            val id = pack.id.orEmpty()
                            PackTile(
                                pack = pack,
                                style = PackTileStyle.Large,
                                sharedKey = packCoverKey(source, id),
                                onClick = { onPackClick(pack, packCoverKey(source, id)) },
                                modifier = Modifier
                                    .padding(horizontal = Spacing.gutter)
                                    .fillMaxWidth()
                                    .staggeredEntrance(index, tracker, id)
                            )
                        }
                    }
                }
            }
            item(key = "nav-space") { Spacer(Modifier.navigationBarsPadding()) }
        }
    }
}
