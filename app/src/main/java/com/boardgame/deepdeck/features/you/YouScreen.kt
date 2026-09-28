package com.boardgame.deepdeck.features.you

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.boardgame.deepdeck.BuildConfig
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.navigation.RootRoute
import com.boardgame.deepdeck.ui.app.LocalBottomBarPadding
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.listLanguageSupport
import com.boardgame.deepdeck.utils.openPlayStoreListing
import com.boardgame.deepdeck.utils.openNotificationSettings
import com.boardgame.deepdeck.utils.openWebPage
import kotlinx.coroutines.launch

private const val TERMS_URL = "https://inclined-scarlet-jn0reqb4.edgeone.dev/"
private const val PRIVACY_URL = "https://uncertain-amethyst-pvpmhpc5.edgeone.dev/"

fun NavGraphBuilder.youTabEntry(navController: NavHostController) {
    composable<RootRoute.You> {
        YouScreen(onLanguageClick = { navController.navigate(RootRoute.Language) })
    }
}

/** You tab: streak & stats, unified language, sound/haptics, reminder, rate, legal. */
@Composable
fun YouScreen(
    onLanguageClick: () -> Unit,
    vm: YouVM = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = com.boardgame.deepdeck.ui.app.LocalSnackbarHostState.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val requestNotifications = com.boardgame.deepdeck.utils.rememberNotificationPermissionRequest { granted ->
        vm.setNotifications(granted)
        if (!granted) {
            scope.launch {
                val result = snackbar.showSnackbar(
                    message = context.getString(R.string.notifications_blocked),
                    actionLabel = context.getString(R.string.open_settings)
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    context.openNotificationSettings()
                }
            }
        }
    }
    LifecycleResumeEffect(Unit) {
        vm.refreshStats()
        onPauseOrDispose { }
    }
    YouContent(
        state = state,
        onLanguageClick = onLanguageClick,
        onSoundChange = { vm.setSound(it) },
        onHapticsChange = { vm.setHaptics(it) },
        onNotificationsChange = { enabled ->
            if (enabled) requestNotifications() else vm.setNotifications(false)
        },
        onRate = { context.openPlayStoreListing() },
        onTerms = { context.openWebPage(TERMS_URL) },
        onPrivacy = { context.openWebPage(PRIVACY_URL) },
    )
}

@Composable
private fun YouContent(
    state: YouUiState,
    onLanguageClick: () -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onRate: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    val bottomInset = LocalBottomBarPadding.current
    val language = listLanguageSupport.find { it.code == state.settings.uiLanguage } ?: listLanguageSupport.first()

    GlowBackground(accent = colors.ember.copy(alpha = 0.8f)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.gutter,
                end = Spacing.gutter,
                bottom = bottomInset + Spacing.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            item(key = "title") {
                Text(
                    stringResource(R.string.tab_you),
                    style = DeepTalkTheme.type.hero,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = Spacing.md)
                )
            }
            item(key = "stats") {
                StatsCard(state, Modifier.staggeredEntrance(0))
            }
            item(key = "prefs") {
                SettingsGroup(stringResource(R.string.you_preferences), Modifier.staggeredEntrance(1)) {
                    SettingRow(
                        icon = Icons.Rounded.Translate,
                        title = stringResource(R.string.language),
                        subtitle = if (state.settings.translateCards) stringResource(R.string.translate_cards_on)
                        else stringResource(R.string.translate_cards_off),
                        onClick = onLanguageClick,
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(language.flagRes),
                                    contentDescription = null,
                                    modifier = Modifier.width(22.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(language.fullName, style = DeepTalkTheme.type.label, color = colors.textSecondary)
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = colors.textMuted
                                )
                            }
                        }
                    )
                    Divider()
                    ToggleRow(Icons.AutoMirrored.Rounded.VolumeUp, stringResource(R.string.enable_sound), null, state.settings.soundEnabled, onSoundChange)
                    Divider()
                    ToggleRow(Icons.Rounded.Vibration, stringResource(R.string.enable_haptics), null, state.settings.hapticsEnabled, onHapticsChange)
                    Divider()
                    ToggleRow(
                        Icons.Rounded.NotificationsNone,
                        stringResource(R.string.evening_reminder),
                        stringResource(R.string.evening_reminder_desc),
                        state.settings.notificationsEnabled,
                        onNotificationsChange
                    )
                }
            }
            item(key = "about") {
                SettingsGroup(stringResource(R.string.about), Modifier.staggeredEntrance(2)) {
                    SettingRow(Icons.Rounded.Star, stringResource(R.string.rate_us), null, onRate)
                    Divider()
                    SettingRow(Icons.Rounded.Description, stringResource(R.string.term_condition), null, onTerms)
                    Divider()
                    SettingRow(Icons.Rounded.PrivacyTip, stringResource(R.string.privacy_policy), null, onPrivacy)
                }
            }
            item(key = "footer") {
                Text(
                    text = stringResource(R.string.version_footer, BuildConfig.VERSION_NAME) + "  ·  " +
                        stringResource(R.string.developer),
                    style = DeepTalkTheme.type.label,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StatsCard(state: YouUiState, modifier: Modifier = Modifier) {
    val colors = DeepTalkTheme.colors
    val active = state.streak.current > 0
    Box(
        modifier
            .fillMaxWidth()
            .glow(colors.ember, radius = 28.dp, alpha = if (active) 0.22f else 0.08f)
            .background(
                Brush.linearGradient(listOf(colors.surfaceHigh, colors.surface)),
                DeepTalkShapes.lg
            )
            .border(1.dp, colors.outline, DeepTalkShapes.lg)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(listOf(colors.gold, colors.ember)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = androidx.compose.ui.res.pluralStringResource(
                            R.plurals.streak_days, state.streak.current, state.streak.current
                        ),
                        style = DeepTalkTheme.type.display,
                        color = colors.textPrimary
                    )
                    Text(
                        text = stringResource(
                            if (state.streak.revealedToday) R.string.streak_done_today else R.string.streak_reveal_today
                        ),
                        style = DeepTalkTheme.type.label,
                        color = if (state.streak.revealedToday) colors.mint else colors.textSecondary
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat(Icons.Rounded.EmojiEvents, state.streak.best.toString(), stringResource(R.string.stat_best_streak), colors.gold, Modifier.weight(1f))
                MiniStat(Icons.Rounded.Style, state.gamesPlayed.toString(), stringResource(R.string.stat_games_played), colors.brand, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniStat(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier
            .background(colors.bgBase.copy(alpha = 0.35f), DeepTalkShapes.md)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, style = DeepTalkTheme.type.headline, color = colors.textPrimary)
            Text(label.uppercase(), style = DeepTalkTheme.type.overline, color = colors.textMuted)
        }
    }
}

@Composable
private fun SettingsGroup(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) {
        Text(
            title.uppercase(),
            style = DeepTalkTheme.type.overline,
            color = DeepTalkTheme.colors.textMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        GlassSurface(Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(color = DeepTalkTheme.colors.outline, modifier = Modifier.padding(start = 56.dp))
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = DeepTalkTheme.colors.textMuted)
    },
) {
    RowShell(icon, title, subtitle, Modifier.pressable(pressedScale = 0.98f, onClick = onClick), trailing)
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = DeepTalkTheme.colors
    RowShell(
        icon, title, subtitle,
        Modifier.pressable(pressedScale = 0.98f, role = Role.Switch) { onCheckedChange(!checked) }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.brandOn,
                checkedTrackColor = colors.brand,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceHigh,
                uncheckedBorderColor = colors.outline,
            )
        )
    }
}

@Composable
private fun RowShell(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    modifier: Modifier,
    trailing: @Composable () -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .background(colors.brand.copy(alpha = 0.14f), DeepTalkShapes.xs),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = colors.brand, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = DeepTalkTheme.type.title.copy(fontSize = DeepTalkTheme.type.body.fontSize), color = colors.textPrimary)
            subtitle?.let { Text(it, style = DeepTalkTheme.type.label, color = colors.textMuted) }
        }
        trailing()
    }
}
