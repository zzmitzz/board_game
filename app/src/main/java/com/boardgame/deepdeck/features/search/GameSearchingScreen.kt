package com.boardgame.deepdeck.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.ui.components.EmptyState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.PackTile
import com.boardgame.deepdeck.ui.components.PackTileStyle
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing

@Composable
fun GameSearchStateful(
    onBackClick: () -> Unit,
    onPackClick: (packId: String) -> Unit,
    viewModel: GameSearchVM = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSearching = uiState is SealedGameSearchUIState.GameSearchUIState || uiState is SealedGameSearchUIState.Loading
    val query by viewModel.query.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadRecentData()
        viewModel.navEvent.collect { event ->
            when (event) {
                is GameSearchNavEvent.NavigateToPackDetail -> onPackClick(event.packId)
            }
        }
    }

    GameSearchingScreen(
        uiState = uiState,
        isSearching = isSearching,
        onBackClick = onBackClick,
        onClearClick = viewModel::clearQuery,
        onQueryChange = viewModel::onQueryChange,
        query = query,
        onPackClick = viewModel::onPackClick,
        onRecentSearchClick = viewModel::onRecentSearchClick
    )
}

/** Search: real text field (focused here, never on Home), recent chips, suggestions, results. */
@Composable
fun GameSearchingScreen(
    uiState: SealedGameSearchUIState,
    isSearching: Boolean,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    query: String = "",
    onPackClick: (packId: String, packTitle: String) -> Unit = { _, _ -> },
    onRecentSearchClick: (String) -> Unit = {}
) {
    GlowBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    onClick = onBackClick
                )
                Spacer(Modifier.width(10.dp))
                SearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    showClear = isSearching || query.isNotEmpty(),
                    onClear = onClearClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(Spacing.md))
            when (uiState) {
                is SealedGameSearchUIState.Loading -> LoadingSection()
                is SealedGameSearchUIState.InitUIState -> InitSection(uiState, onPackClick, onRecentSearchClick)
                is SealedGameSearchUIState.GameSearchUIState -> ResultsSection(
                    title = stringResource(R.string.search_results),
                    packs = uiState.results,
                    onPackClick = onPackClick
                )
                is SealedGameSearchUIState.Error -> EmptyState(
                    title = stringResource(R.string.no_packs_found),
                    message = uiState.message,
                    icon = Icons.Rounded.SearchOff
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    showClear: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        modifier = modifier
            .height(48.dp)
            .background(colors.glass, DeepTalkShapes.pill)
            .border(1.dp, colors.outline, DeepTalkShapes.pill)
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(stringResource(R.string.search_game_placeholder), style = DeepTalkTheme.type.body, color = colors.textMuted)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = DeepTalkTheme.type.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brand),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        if (showClear) {
            Box(
                Modifier
                    .size(36.dp)
                    .pressable(onClick = onClear),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Close, stringResource(R.string.cancel_text), tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Column(
        Modifier.padding(horizontal = Spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(5) { SkeletonBox(Modifier.fillMaxWidth().height(84.dp), DeepTalkShapes.md) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InitSection(
    state: SealedGameSearchUIState.InitUIState,
    onPackClick: (packId: String, packTitle: String) -> Unit,
    onRecentSearchClick: (String) -> Unit
) {
    val colors = DeepTalkTheme.colors
    LazyColumn(
        contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "recent-title") {
            Text(stringResource(R.string.recent), style = DeepTalkTheme.type.overline, color = colors.textMuted)
        }
        item(key = "recent") {
            if (state.recentSearch.isEmpty()) {
                Text(stringResource(R.string.no_recent_search), style = DeepTalkTheme.type.body, color = colors.textSecondary)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.recentSearch.forEach { recent ->
                        Row(
                            Modifier
                                .background(colors.glass, DeepTalkShapes.pill)
                                .border(1.dp, colors.outline, DeepTalkShapes.pill)
                                .pressable { onRecentSearchClick(recent) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.History, null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(recent, style = DeepTalkTheme.type.label, color = colors.textPrimary)
                        }
                    }
                }
            }
        }
        if (state.suggestPacks.isNotEmpty()) {
            item(key = "suggested-title") {
                Text(
                    stringResource(R.string.suggested),
                    style = DeepTalkTheme.type.overline,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = Spacing.md)
                )
            }
            itemsIndexed(state.suggestPacks, key = { _, p -> "s-${p.id}" }) { index, pack ->
                PackTile(
                    pack = pack,
                    style = PackTileStyle.Row,
                    onClick = { onPackClick(pack.id.orEmpty(), pack.title.orEmpty()) },
                    modifier = Modifier.staggeredEntrance(index)
                )
            }
        }
    }
}

@Composable
private fun ResultsSection(
    title: String,
    packs: List<PacksPreview>,
    onPackClick: (packId: String, packTitle: String) -> Unit
) {
    if (packs.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.no_packs_found),
            message = stringResource(R.string.search_no_results),
            icon = Icons.Rounded.SearchOff
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "title") {
            Text(title, style = DeepTalkTheme.type.overline, color = DeepTalkTheme.colors.textMuted)
        }
        itemsIndexed(packs, key = { _, p -> "r-${p.id}" }) { index, pack ->
            PackTile(
                pack = pack,
                style = PackTileStyle.Row,
                onClick = { onPackClick(pack.id.orEmpty(), pack.title.orEmpty()) },
                modifier = Modifier.staggeredEntrance(index)
            )
        }
    }
}
