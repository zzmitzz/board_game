package com.boardgame.deepdeck.features.library

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.local.entity.LocalPackEntity
import com.boardgame.deepdeck.data.local.entity.SavedCardEntity
import com.boardgame.deepdeck.data.model.CardLevel
import com.boardgame.deepdeck.features.ingame.startGame
import com.boardgame.deepdeck.features.mylibrary.MyLibraryRoute
import com.boardgame.deepdeck.navigation.RootRoute
import com.boardgame.deepdeck.ui.app.LocalBottomBarPadding
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.EmptyState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.OverlinePill
import com.boardgame.deepdeck.ui.components.PackCover
import com.boardgame.deepdeck.ui.components.PlayerAvatar
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.SegmentedControl
import com.boardgame.deepdeck.ui.components.color
import com.boardgame.deepdeck.ui.components.icon
import com.boardgame.deepdeck.ui.components.label
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.shareImage

fun NavGraphBuilder.libraryTabEntry(navController: NavHostController) {
    composable<RootRoute.Library> {
        LibraryTabScreen(
            onAddPackClick = { navController.navigate(MyLibraryRoute.AddPack) },
            onOpenPack = { id -> navController.navigate(MyLibraryRoute.PackCards(id)) },
            onEditPack = { id -> navController.navigate(MyLibraryRoute.EditPack(id)) },
            onPlayPack = { id, isCustom -> navController.startGame(id, isCustom = isCustom, quickPlay = true) },
        )
    }
}

@Composable
fun LibraryTabScreen(
    onAddPackClick: () -> Unit,
    onOpenPack: (String) -> Unit,
    onEditPack: (String) -> Unit,
    onPlayPack: (id: String, isCustom: Boolean) -> Unit,
    initialSegment: Int = 0,
    vm: LibraryVM = hiltViewModel(),
) {
    val colors = DeepTalkTheme.colors
    val bottomInset = LocalBottomBarPadding.current
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    var segment by rememberSaveable { mutableIntStateOf(initialSegment) }
    var packToDelete by remember { mutableStateOf<LocalPackEntity?>(null) }

    LifecycleResumeEffect(vm) {
        vm.refreshHistory()
        onPauseOrDispose { }
    }
    LaunchedEffect(vm) {
        vm.uiEffect.collect { effect ->
            when (effect) {
                is LibraryUiEffect.Share -> context.shareImage(
                    effect.uri,
                    chooserTitle = context.getString(R.string.share),
                    fallbackText = effect.fallbackText
                )
                is LibraryUiEffect.CardRemoved -> {
                    val result = snackbar.showSnackbar(
                        message = context.getString(R.string.saved_card_removed),
                        actionLabel = context.getString(R.string.undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) vm.restoreSaved(effect.card)
                }
                is LibraryUiEffect.ShowMessage -> snackbar.showSnackbar(context.getString(effect.message))
            }
        }
    }

    val segments = listOf(
        stringResource(R.string.library_saved),
        stringResource(R.string.library_my_packs),
        stringResource(R.string.library_history)
    )
    GlowBackground(accent = colors.rose.copy(alpha = 0.8f)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.gutter)
                    .padding(top = Spacing.md, bottom = Spacing.md)
            ) {
                Text(stringResource(R.string.tab_library), style = DeepTalkTheme.type.hero, color = colors.textPrimary)
                Spacer(Modifier.height(Spacing.md))
                SegmentedControl(options = segments, selectedIndex = segment, onSelect = { segment = it })
            }
            AnimatedContent(
                targetState = segment,
                transitionSpec = { Motion.fadeThroughEnter() togetherWith Motion.fadeThroughExit() },
                modifier = Modifier.weight(1f),
                label = "librarySegment"
            ) { index ->
                Box(Modifier.fillMaxSize()) {
                    when (index) {
                        0 -> SavedList(uiState.saved, bottomInset, onShare = vm::shareSaved, onRemove = vm::removeSaved)
                        1 -> PacksList(
                            packs = uiState.packs,
                            bottomInset = bottomInset,
                            onAddPack = onAddPackClick,
                            onOpenPack = onOpenPack,
                            onEditPack = onEditPack,
                            onPlay = { onPlayPack(it, true) },
                            onDelete = { packToDelete = it },
                        )
                        else -> HistoryList(
                            items = uiState.history,
                            loaded = uiState.historyLoaded,
                            bottomInset = bottomInset,
                            onReplay = { item -> onPlayPack(item.result.packID, item.isCustomPack) }
                        )
                    }
                }
            }
        }
    }

    packToDelete?.let { pack ->
        AlertDialog(
            onDismissRequest = { packToDelete = null },
            containerColor = colors.surfaceHigh,
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = colors.rose) },
            title = { Text(stringResource(R.string.delete_pack_title), style = DeepTalkTheme.type.headline, color = colors.textPrimary) },
            text = {
                Text(
                    stringResource(R.string.delete_pack_message, pack.title),
                    style = DeepTalkTheme.type.body,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deletePack(pack)
                    packToDelete = null
                }) { Text(stringResource(R.string.delete), color = colors.rose) }
            },
            dismissButton = {
                TextButton(onClick = { packToDelete = null }) {
                    Text(stringResource(R.string.cancel_text), color = colors.brand)
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Saved ♥
// ---------------------------------------------------------------------------

@Composable
private fun SavedList(
    cards: List<SavedCardEntity>,
    bottomInset: Dp,
    onShare: (SavedCardEntity) -> Unit,
    onRemove: (SavedCardEntity) -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val tracker = rememberEntranceTracker()
    if (cards.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.saved_empty_title),
            message = stringResource(R.string.saved_empty_message),
            icon = Icons.Rounded.FavoriteBorder,
            modifier = Modifier.padding(top = Spacing.xxl)
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = bottomInset + Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(cards, key = { _, c -> c.id }) { index, card ->
            val level = CardLevel.from(card.level)
            GlassSurface(
                Modifier
                    .fillMaxWidth()
                    .animateItem()
                    .staggeredEntrance(index, tracker, card.id),
                tint = level.color
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OverlinePill(level.label(), level.color, icon = level.icon)
                        Spacer(Modifier.weight(1f))
                        GlassIconButton(
                            icon = Icons.Rounded.IosShare,
                            contentDescription = stringResource(R.string.share_card_image),
                            size = 36.dp,
                            onClick = { onShare(card) }
                        )
                        Spacer(Modifier.size(8.dp))
                        GlassIconButton(
                            icon = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.delete),
                            size = 36.dp,
                            tint = colors.rose,
                            onClick = { onRemove(card) }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(card.text, style = DeepTalkTheme.type.displaySmall, color = colors.textPrimary)
                    card.back?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("↻ $it", style = DeepTalkTheme.type.body, color = colors.textSecondary)
                    }
                    card.packTitle?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = DeepTalkTheme.type.label, color = colors.textMuted)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// My packs
// ---------------------------------------------------------------------------

@Composable
private fun PacksList(
    packs: List<CustomPackUi>,
    bottomInset: Dp,
    onAddPack: () -> Unit,
    onOpenPack: (String) -> Unit,
    onEditPack: (String) -> Unit,
    onPlay: (String) -> Unit,
    onDelete: (LocalPackEntity) -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val tracker = rememberEntranceTracker()
    LazyColumn(
        contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = bottomInset + Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "create") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(DeepTalkShapes.lg)
                    .border(1.dp, colors.brand.copy(alpha = 0.45f), DeepTalkShapes.lg)
                    .background(colors.brand.copy(alpha = 0.08f))
                    .pressable(onClick = onAddPack)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(DeepTalkShapes.sm)
                        .background(colors.brandGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.brandOn)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.make_your_own_deck), style = DeepTalkTheme.type.title, color = colors.textPrimary)
                    Text(stringResource(R.string.make_your_own_deck_desc), style = DeepTalkTheme.type.label, color = colors.textSecondary)
                }
            }
        }
        if (packs.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    title = stringResource(R.string.no_packs_yet),
                    message = stringResource(R.string.library_packs_empty_message),
                    icon = Icons.Rounded.Style,
                    modifier = Modifier.padding(top = Spacing.xl)
                )
            }
        }
        itemsIndexed(packs, key = { _, p -> p.pack.id }) { index, item ->
            CustomPackRow(
                item = item,
                modifier = Modifier
                    .animateItem()
                    .staggeredEntrance(index, tracker, item.pack.id),
                onOpen = { onOpenPack(item.pack.id) },
                onEdit = { onEditPack(item.pack.id) },
                onPlay = { onPlay(item.pack.id) },
                onDelete = { onDelete(item.pack) },
            )
        }
    }
}

@Composable
private fun CustomPackRow(
    item: CustomPackUi,
    modifier: Modifier,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    var menu by remember { mutableStateOf(false) }
    val playable = item.cardCount > 0
    GlassSurface(modifier.fillMaxWidth()) {
        Column(Modifier.pressable(pressedScale = 0.98f, onClick = onOpen).padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PackCover(
                    imageUrl = item.pack.coverImageUri,
                    title = item.pack.title,
                    accent = colors.brand,
                    modifier = Modifier.size(64.dp),
                    shape = DeepTalkShapes.md
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.pack.title,
                        style = DeepTalkTheme.type.title,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        pluralStringResource(R.plurals.cards_count, item.cardCount, item.cardCount),
                        style = DeepTalkTheme.type.label,
                        color = colors.textSecondary
                    )
                    if (item.pack.tag.isNotBlank()) {
                        Text(item.pack.tag, style = DeepTalkTheme.type.label, color = colors.textMuted, maxLines = 1)
                    }
                }
                Box {
                    GlassIconButton(Icons.Rounded.MoreVert, stringResource(R.string.pack_options), { menu = true }, size = 36.dp)
                    DropdownMenu(
                        expanded = menu,
                        onDismissRequest = { menu = false },
                        containerColor = colors.surfaceHigh
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_cards), color = colors.textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.EditNote, null, tint = colors.textSecondary) },
                            onClick = { menu = false; onOpen() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_pack), color = colors.textPrimary) },
                            leadingIcon = { Icon(Icons.Rounded.Tune, null, tint = colors.textSecondary) },
                            onClick = { menu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete), color = colors.rose) },
                            leadingIcon = { Icon(Icons.Rounded.DeleteOutline, null, tint = colors.rose) },
                            onClick = { menu = false; onDelete() }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (playable) {
                PrimaryButton(
                    text = stringResource(R.string.play),
                    onClick = onPlay,
                    leadingIcon = Icons.Rounded.PlayArrow,
                    height = 44.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    stringResource(R.string.pack_needs_cards),
                    style = DeepTalkTheme.type.label,
                    color = colors.textMuted
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// History
// ---------------------------------------------------------------------------

@Composable
private fun HistoryList(
    items: List<HistoryItemUi>,
    loaded: Boolean,
    bottomInset: Dp,
    onReplay: (HistoryItemUi) -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val tracker = rememberEntranceTracker()
    if (loaded && items.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.no_games_played_yet),
            message = stringResource(R.string.history_empty_message),
            icon = Icons.Rounded.History,
            modifier = Modifier.padding(top = Spacing.xxl)
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = bottomInset + Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(items, key = { _, it -> it.result.id }) { index, item ->
            val winner = item.ranked.firstOrNull()
            GlassSurface(
                Modifier
                    .fillMaxWidth()
                    .staggeredEntrance(index, tracker, item.result.id)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.result.packName.ifBlank { stringResource(R.string.board_game) },
                                style = DeepTalkTheme.type.title,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                DateUtils.getRelativeTimeSpanString(item.result.timeStamp).toString(),
                                style = DeepTalkTheme.type.label,
                                color = colors.textMuted
                            )
                        }
                        if (item.canReplay) {
                            GlassIconButton(
                                icon = Icons.Rounded.Replay,
                                contentDescription = stringResource(R.string.play_again),
                                onClick = { onReplay(item) },
                                size = 40.dp,
                                tint = colors.brand,
                                active = true
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            item.ranked.take(5).forEachIndexed { i, entry ->
                                PlayerAvatar(
                                    entry.player,
                                    size = 28.dp,
                                    ring = colors.bgBase,
                                    modifier = Modifier.offset(x = (i * 18).dp)
                                )
                            }
                            Spacer(Modifier.width((28 + (item.ranked.take(5).size - 1).coerceAtLeast(0) * 18).dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        winner?.let {
                            Text(
                                "🏆 " + stringResource(R.string.history_winner, it.player.name, it.score.getScore()),
                                style = DeepTalkTheme.type.label,
                                color = colors.gold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
