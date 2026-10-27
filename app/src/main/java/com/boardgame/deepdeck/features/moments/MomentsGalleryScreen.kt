package com.boardgame.deepdeck.features.moments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.entity.MomentEntity
import com.boardgame.deepdeck.data.repository.MomentRepository
import com.boardgame.deepdeck.ui.components.EmptyState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MomentsGalleryVM @Inject constructor(
    repository: MomentRepository,
) : ViewModel() {
    /** Every moment, newest first; null until the first Room emission. */
    val moments: StateFlow<List<MomentEntity>?> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

/** All captured moments (You → Captured moments): adaptive grid, newest first. */
@Composable
fun MomentsGalleryScreen(
    vm: MomentsGalleryVM,
    onBackClick: () -> Unit,
    onOpenMoment: (String) -> Unit,
) {
    val moments by vm.moments.collectAsStateWithLifecycle()
    val colors = DeepTalkTheme.colors
    val tracker = rememberEntranceTracker()

    GlowBackground(accent = colors.mint.copy(alpha = 0.8f)) {
        LazyVerticalGrid(
            // 2 columns on phones in portrait, more in landscape / on tablets.
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = Spacing.xxl),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "top", span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    Modifier
                        .statusBarsPadding()
                        .padding(top = Spacing.xs)
                ) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        onClick = onBackClick
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Text(stringResource(R.string.captured_moments), style = DeepTalkTheme.type.hero, color = colors.textPrimary)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        stringResource(R.string.moments_gallery_subtitle),
                        style = DeepTalkTheme.type.body,
                        color = colors.textSecondary
                    )
                }
            }
            val list = moments
            if (list != null && list.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = stringResource(R.string.moments_empty_title),
                        message = stringResource(R.string.moments_empty_message),
                        icon = Icons.Rounded.PhotoLibrary,
                        modifier = Modifier.padding(top = Spacing.xl)
                    )
                }
            }
            itemsIndexed(list.orEmpty(), key = { _, it -> it.id }) { index, moment ->
                MomentGridItem(
                    moment = moment,
                    onClick = { onOpenMoment(moment.id) },
                    modifier = Modifier
                        .animateItem()
                        .staggeredEntrance(index, tracker, moment.id)
                )
            }
            item(key = "bottom", span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun MomentGridItem(moment: MomentEntity, onClick: () -> Unit, modifier: Modifier) {
    val colors = DeepTalkTheme.colors
    val context = LocalContext.current
    Column(modifier) {
        MomentThumbnail(
            moment = moment,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            formatMomentDate(context, moment.createdAt),
            style = DeepTalkTheme.type.label,
            color = colors.textMuted,
            maxLines = 1
        )
        moment.playerName?.let {
            Text(
                stringResource(R.string.player_turn_format, it),
                style = DeepTalkTheme.type.title,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        moment.cardText?.let {
            Text(
                it,
                style = DeepTalkTheme.type.label,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
