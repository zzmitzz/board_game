package com.boardgame.deepdeck.features.gamesetup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.ingame.model.GameConfig
import com.boardgame.deepdeck.ui.app.LocalSnackbarHostState
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.PlayerAvatar
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.Stepper
import com.boardgame.deepdeck.ui.components.ToggleRow
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.rememberEntranceTracker
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.model.GamePlayer
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing

/**
 * Stateful Setup lobby. [onStartGame] receives the validated [GameConfig]; the nav entry
 * hands it to the game-flow-scoped `InGameVM`.
 */
@Composable
fun GameSetupScreenStateful(
    packId: String,
    isCustomPack: Boolean,
    quickPlay: Boolean,
    vm: GameSetupVM,
    onBackClick: () -> Unit,
    onStartGame: (GameConfig) -> Unit,
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    val context = LocalContext.current
    // Debounce double taps on Start (state that survives recomposition).
    var lastStartClick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(packId) { vm.bind(packId, isCustomPack, quickPlay) }
    LaunchedEffect(vm) {
        vm.uiEffect.collect { effect ->
            when (effect) {
                is GameSetupUiEffect.ShowMessage -> snackbar.showSnackbar(
                    if (effect.arg != null) context.getString(effect.message, effect.arg)
                    else context.getString(effect.message)
                )
                is GameSetupUiEffect.StartGame -> onStartGame(effect.config)
            }
        }
    }

    GameSetupScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddPlayer = vm::addPlayer,
        onRemovePlayer = vm::removePlayer,
        onRenamePlayer = vm::renamePlayer,
        onCycleAvatar = vm::cycleAvatar,
        onClearPlayers = vm::clearPlayers,
        onRoundsChange = vm::setRounds,
        onSpeedModeChange = vm::setSpeedMode,
        onPenaltyChange = vm::setPenaltyEnabled,
        onPenaltyTextChange = vm::setPenaltyText,
        onPassPhoneChange = vm::setPassThePhone,
        onStart = {
            val now = System.currentTimeMillis()
            if (now - lastStartClick > START_DEBOUNCE_MS) {
                lastStartClick = now
                vm.onStartClick()
            }
        },
    )
}

private const val START_DEBOUNCE_MS = 800L

@Composable
fun GameSetupScreen(
    uiState: GameSetupUiState,
    onBackClick: () -> Unit = {},
    onAddPlayer: (String) -> Unit = {},
    onRemovePlayer: (Int) -> Unit = {},
    onRenamePlayer: (Int, String) -> Unit = { _, _ -> },
    onCycleAvatar: (Int) -> Unit = {},
    onClearPlayers: () -> Unit = {},
    onRoundsChange: (Int) -> Unit = {},
    onSpeedModeChange: (Boolean) -> Unit = {},
    onPenaltyChange: (Boolean) -> Unit = {},
    onPenaltyTextChange: (String) -> Unit = {},
    onPassPhoneChange: (Boolean) -> Unit = {},
    onStart: () -> Unit = {},
) {
    val colors = DeepTalkTheme.colors
    val tracker = rememberEntranceTracker()
    var rulesExpanded by rememberSaveable { mutableStateOf(false) }

    GlowBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top bar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back),
                    onClick = onBackClick
                )
                Spacer(Modifier.width(Spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.setup_your_game).uppercase(),
                        style = DeepTalkTheme.type.overline,
                        color = colors.brand
                    )
                    Text(
                        text = uiState.packTitle ?: stringResource(R.string.board_game),
                        style = DeepTalkTheme.type.displaySmall,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = Spacing.gutter, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item(key = "players-header") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.whos_playing),
                            style = DeepTalkTheme.type.headline,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            stringResource(R.string.setup_player_count, uiState.players.size, GameConfig.MAX_PLAYERS),
                            style = DeepTalkTheme.type.label,
                            color = colors.textSecondary
                        )
                        if (uiState.players.isNotEmpty()) {
                            Spacer(Modifier.width(Spacing.xs))
                            Text(
                                stringResource(R.string.setup_clear_players),
                                style = DeepTalkTheme.type.label,
                                color = colors.rose,
                                modifier = Modifier
                                    .clip(DeepTalkShapes.pill)
                                    .pressable(onClick = onClearPlayers)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                itemsIndexed(uiState.players, key = { _, p -> "player-${p.id}" }) { index, player ->
                    PlayerRow(
                        player = player,
                        modifier = Modifier
                            .animateItem()
                            .staggeredEntrance(index, tracker, "player-${player.id}"),
                        onRename = { onRenamePlayer(player.id, it) },
                        onRemove = { onRemovePlayer(player.id) },
                        onCycleAvatar = { onCycleAvatar(player.id) },
                    )
                }
                item(key = "add-player") {
                    AddPlayerRow(enabled = uiState.canAddPlayer, onAdd = onAddPlayer)
                }
                if (uiState.players.size < GameConfig.MIN_PLAYERS && !uiState.isLoading) {
                    item(key = "min-hint") {
                        Text(
                            stringResource(R.string.setup_min_players),
                            style = DeepTalkTheme.type.label,
                            color = colors.textMuted,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                item(key = "rules") {
                    Spacer(Modifier.height(Spacing.sm))
                    HouseRulesCard(
                        uiState = uiState,
                        expanded = rulesExpanded,
                        onToggleExpanded = { rulesExpanded = !rulesExpanded },
                        onRoundsChange = onRoundsChange,
                        onSpeedModeChange = onSpeedModeChange,
                        onPenaltyChange = onPenaltyChange,
                        onPenaltyTextChange = onPenaltyTextChange,
                        onPassPhoneChange = onPassPhoneChange,
                    )
                }
                item(key = "bottom-space") { Spacer(Modifier.height(Spacing.xl)) }
            }

            // Sticky start CTA (pulses when arriving from Quick Play).
            val pulse = if (uiState.quickPlay && uiState.canStart && !DeepTalkTheme.reducedMotion) {
                val transition = rememberInfiniteTransition(label = "startPulse")
                transition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.03f,
                    animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
                    label = "startPulseScale"
                ).value
            } else 1f
            PrimaryButton(
                text = stringResource(R.string.start_game),
                onClick = onStart,
                enabled = uiState.canStart,
                leadingIcon = Icons.Rounded.PlayArrow,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = Spacing.gutter, vertical = Spacing.md)
                    .then(if (pulse != 1f) Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse } else Modifier)
            )
        }
    }
}

@Composable
private fun PlayerRow(
    player: GamePlayer,
    modifier: Modifier,
    onRename: (String) -> Unit,
    onRemove: () -> Unit,
    onCycleAvatar: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val changeAvatar = stringResource(R.string.setup_change_avatar)
    GlassSurface(modifier.fillMaxWidth(), shape = DeepTalkShapes.md) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(
                player = player,
                size = 40.dp,
                modifier = Modifier
                    .pressable(pressedScale = 0.88f, onClickLabel = changeAvatar, onClick = onCycleAvatar)
                    .semantics { contentDescription = changeAvatar }
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = player.name,
                onValueChange = onRename,
                singleLine = true,
                textStyle = DeepTalkTheme.type.title.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brand),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f)
            )
            GlassIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.delete_player_named, player.name),
                onClick = onRemove,
                size = 34.dp,
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun AddPlayerRow(enabled: Boolean, onAdd: (String) -> Unit) {
    val colors = DeepTalkTheme.colors
    var name by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (enabled) {
            onAdd(name)
            name = ""
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(DeepTalkShapes.md)
            .border(1.dp, colors.brand.copy(alpha = 0.35f), DeepTalkShapes.md)
            .background(colors.brand.copy(alpha = 0.06f))
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            if (name.isEmpty()) {
                Text(
                    stringResource(if (enabled) R.string.setup_add_player_hint else R.string.setup_roster_full),
                    style = DeepTalkTheme.type.body,
                    color = colors.textMuted
                )
            }
            BasicTextField(
                value = name,
                onValueChange = { name = it.take(24) },
                enabled = enabled,
                singleLine = true,
                textStyle = DeepTalkTheme.type.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brand),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        GlassIconButton(
            icon = Icons.Rounded.PersonAdd,
            contentDescription = stringResource(R.string.setup_add_player),
            onClick = submit,
            size = 40.dp,
            tint = colors.brand,
            active = enabled
        )
    }
}

@Composable
private fun HouseRulesCard(
    uiState: GameSetupUiState,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onRoundsChange: (Int) -> Unit,
    onSpeedModeChange: (Boolean) -> Unit,
    onPenaltyChange: (Boolean) -> Unit,
    onPenaltyTextChange: (String) -> Unit,
    onPassPhoneChange: (Boolean) -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, label = "rulesChevron")
    val summary = listOfNotNull(
        pluralStringResource(R.plurals.rounds_count, uiState.rounds, uiState.rounds),
        stringResource(R.string.rule_pass_phone_short).takeIf { uiState.passThePhone },
        stringResource(R.string.rule_speed_short).takeIf { uiState.speedMode },
        stringResource(R.string.rule_penalty_short).takeIf { uiState.penaltyEnabled },
    ).joinToString(" · ")

    GlassSurface(
        Modifier
            .fillMaxWidth()
            .animateContentSize(),
        tint = colors.brand
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .pressable(pressedScale = 0.98f, role = Role.Button, onClick = onToggleExpanded)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Tune, contentDescription = null, tint = colors.brand)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.house_rules), style = DeepTalkTheme.type.title, color = colors.textPrimary)
                    Text(summary, style = DeepTalkTheme.type.label, color = colors.textSecondary)
                }
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = stringResource(if (expanded) R.string.collapse else R.string.expand),
                    tint = colors.textSecondary,
                    modifier = Modifier.rotate(chevron)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    HorizontalDivider(color = colors.outline)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.number_of_rounds), style = DeepTalkTheme.type.title, color = colors.textPrimary)
                            Text(
                                stringResource(R.string.rounds_hint, uiState.players.size.coerceAtLeast(1) * uiState.rounds),
                                style = DeepTalkTheme.type.label,
                                color = colors.textSecondary
                            )
                        }
                        Stepper(
                            value = uiState.rounds,
                            onValueChange = onRoundsChange,
                            range = GameConfig.MIN_ROUNDS..GameConfig.MAX_ROUNDS,
                            decrementLabel = stringResource(R.string.rounds_decrease),
                            incrementLabel = stringResource(R.string.rounds_increase),
                        )
                    }
                    HorizontalDivider(color = colors.outline)
                    ToggleRow(
                        icon = Icons.Rounded.PhoneAndroid,
                        title = stringResource(R.string.rule_pass_phone),
                        description = stringResource(R.string.rule_pass_phone_desc),
                        checked = uiState.passThePhone,
                        onCheckedChange = onPassPhoneChange,
                        accent = colors.sky
                    )
                    ToggleRow(
                        icon = Icons.Rounded.Timer,
                        title = stringResource(R.string.speed_mode),
                        description = stringResource(R.string.speed_mode_desc),
                        checked = uiState.speedMode,
                        onCheckedChange = onSpeedModeChange,
                        accent = colors.gold
                    )
                    ToggleRow(
                        icon = Icons.Rounded.Gavel,
                        title = stringResource(R.string.rule_penalty),
                        description = stringResource(if (uiState.penaltyEnabled) R.string.penalty_on_skip else R.string.no_penalty),
                        checked = uiState.penaltyEnabled,
                        onCheckedChange = onPenaltyChange,
                        accent = colors.rose
                    )
                    AnimatedVisibility(visible = uiState.penaltyEnabled) {
                        Box(
                            Modifier
                                .padding(start = 64.dp, end = 16.dp, bottom = 16.dp)
                                .fillMaxWidth()
                                .clip(DeepTalkShapes.sm)
                                .background(colors.surfaceHigh)
                                .padding(12.dp)
                        ) {
                            if (uiState.penaltyText.isEmpty()) {
                                Text(stringResource(R.string.penalty_hint), style = DeepTalkTheme.type.body, color = colors.textMuted)
                            }
                            BasicTextField(
                                value = uiState.penaltyText,
                                onValueChange = onPenaltyTextChange,
                                textStyle = DeepTalkTheme.type.body.copy(color = colors.textPrimary),
                                cursorBrush = SolidColor(colors.rose),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
