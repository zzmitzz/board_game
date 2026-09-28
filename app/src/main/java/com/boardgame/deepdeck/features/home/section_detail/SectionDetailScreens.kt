package com.boardgame.deepdeck.features.home.section_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.ui.components.EmptyState
import com.boardgame.deepdeck.ui.components.ErrorState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.PackTile
import com.boardgame.deepdeck.ui.components.PackTileStyle
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.packCoverKey
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing


@Composable
fun SectionDetailScreens(
    initialTitle: String?,
    onBackClick: () -> Unit = {},
    onCardClick: (pack: PacksPreview, coverKey: String?) -> Unit,
    vm: SectionScreenScopedVM = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    SectionDetailStateless(
        initialTitle = initialTitle,
        onBackClick = onBackClick,
        uiState = uiState,
        onCardClick = onCardClick,
        onRetry = vm::load
    )
}

/** Full section list (LazyColumn — no truncation) with skeleton, empty and error states. */
@Composable
fun SectionDetailStateless(
    initialTitle: String?,
    onBackClick: () -> Unit,
    uiState: SectionUIState,
    onCardClick: (PacksPreview, String?) -> Unit,
    onRetry: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val tracker = rememberEntranceTracker()
    val success = uiState as? SectionUIState.Success
    val title = success?.sectionEntity?.name ?: initialTitle.orEmpty()
    val sectionId = success?.sectionEntity?.id ?: "detail"
    val source = "section-detail-$sectionId"

    GlowBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "top") {
                Column(Modifier.statusBarsPadding().padding(bottom = Spacing.md)) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        onClick = onBackClick
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Text(title, style = DeepTalkTheme.type.hero, color = colors.textPrimary)
                    val description = success?.sectionEntity?.description?.takeIf { it.isNotBlank() }
                    val count = success?.packs?.size
                    val subtitle = description ?: count?.let { pluralStringResource(R.plurals.packs_count, it, it) }
                    if (subtitle != null) {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(subtitle, style = DeepTalkTheme.type.body, color = colors.textSecondary)
                    }
                }
            }
            when (uiState) {
                SectionUIState.Loading -> items(6) {
                    SkeletonBox(Modifier.fillMaxWidth().height(84.dp), DeepTalkShapes.md)
                }

                is SectionUIState.Error -> item(key = "error") { ErrorState(onRetry = onRetry) }

                is SectionUIState.Success -> if (uiState.packs.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = stringResource(R.string.no_packs_found),
                            message = stringResource(R.string.section_empty_message),
                            icon = Icons.Rounded.Inbox
                        )
                    }
                } else {
                    itemsIndexed(uiState.packs, key = { _, p -> p.id.orEmpty() }) { index, pack ->
                        val id = pack.id.orEmpty()
                        PackTile(
                            pack = pack,
                            style = PackTileStyle.Row,
                            sharedKey = packCoverKey(source, id),
                            onClick = { onCardClick(pack, packCoverKey(source, id)) },
                            modifier = Modifier.staggeredEntrance(index, tracker, id)
                        )
                    }
                }
            }
            item(key = "nav-space") { Spacer(Modifier.navigationBarsPadding().height(Spacing.xl)) }
        }
    }
}
