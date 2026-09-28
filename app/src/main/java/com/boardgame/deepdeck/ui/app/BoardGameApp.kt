package com.boardgame.deepdeck.ui.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.navigation.LocalNavController
import com.boardgame.deepdeck.navigation.NavigationGraph
import com.boardgame.deepdeck.navigation.TopLevelTab
import com.boardgame.deepdeck.navigation.navigateToTab
import com.boardgame.deepdeck.ui.components.LocalDeepTalkHaptics
import com.boardgame.deepdeck.ui.components.OfflineBanner
import com.boardgame.deepdeck.ui.components.glow
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.components.rememberDeepTalkHaptics
import com.boardgame.deepdeck.ui.state.BoardGameAppState
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme


val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

/** Bottom space tab-root screens must leave for the floating nav bar (incl. system nav inset). */
val LocalBottomBarPadding = compositionLocalOf { 0.dp }

private val NavBarHeight = 64.dp

/**
 * App shell: NavHost + floating glass bottom nav (Play / Library / You, hidden
 * outside tab roots) + non-blocking offline banner + snackbar host.
 */
@Composable
fun BoardGameApp(
    appState: BoardGameAppState,
    hapticsEnabled: Boolean,
) {
    val navController = rememberNavController()
    val snackBarState = remember { SnackbarHostState() }
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = TopLevelTab.rootOf(backStackEntry?.destination)
    val hapticsOn by rememberUpdatedState(hapticsEnabled)
    val haptics = rememberDeepTalkHaptics { hapticsOn }

    val systemNavBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarPadding: Dp = NavBarHeight + 12.dp + systemNavBottom

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackBarState,
        LocalNavController provides navController,
        LocalDeepTalkHaptics provides haptics,
        LocalBottomBarPadding provides bottomBarPadding,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepTalkTheme.colors.bgBase)
        ) {
            NavigationGraph(navController = navController, modifier = Modifier.fillMaxSize())

            OfflineBanner(
                visible = isOffline,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
                actionLabel = if (currentTab != TopLevelTab.LIBRARY) stringResource(R.string.tab_library) else null,
                onAction = { navController.navigateToTab(TopLevelTab.LIBRARY) }
            )

            AnimatedVisibility(
                visible = currentTab != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                DeepTalkBottomBar(
                    selected = currentTab ?: TopLevelTab.PLAY,
                    onSelect = { tab ->
                        if (tab != currentTab) navController.navigateToTab(tab)
                    }
                )
            }

            SnackbarHost(
                hostState = snackBarState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (currentTab != null) bottomBarPadding else systemNavBottom + 16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DeepTalkTheme.colors.surfaceHigh,
                    contentColor = DeepTalkTheme.colors.textPrimary,
                    actionColor = DeepTalkTheme.colors.brand,
                    shape = DeepTalkShapes.md
                )
            }
        }
    }
}

/** Floating glass pill with animated brand indicator. */
@Composable
private fun DeepTalkBottomBar(
    selected: TopLevelTab,
    onSelect: (TopLevelTab) -> Unit,
) {
    val colors = DeepTalkTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(colors.bgBase.copy(alpha = 0f), colors.bgBase.copy(alpha = 0.9f)))
            )
            // Swallow touches in the bar area so taps between items never reach content below.
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 8.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(NavBarHeight)
                .glow(colors.brandStrong, radius = 24.dp, alpha = 0.18f, offsetY = 0.dp)
                .clip(DeepTalkShapes.pill)
                .background(colors.bgElevated)
                .border(1.dp, colors.outline, DeepTalkShapes.pill)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopLevelTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val weight by animateFloatAsState(if (isSelected) 1.5f else 1f, label = "tabWeight")
                val label = stringResource(tab.label)
                Box(
                    Modifier
                        .weight(weight)
                        .height(NavBarHeight - 12.dp)
                        .clip(DeepTalkShapes.pill)
                        .background(
                            if (isSelected) Brush.linearGradient(listOf(colors.brand, colors.brandStrong))
                            else Brush.linearGradient(listOf(colors.bgElevated.copy(alpha = 0f), colors.bgElevated.copy(alpha = 0f)))
                        )
                        .semantics { this.selected = isSelected }
                        .pressable(role = Role.Tab, pressedScale = 0.94f, onClickLabel = label) { onSelect(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                            contentDescription = if (isSelected) null else label,
                            tint = if (isSelected) colors.brandOn else colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        AnimatedVisibility(visible = isSelected) {
                            Row {
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = DeepTalkTheme.type.label, color = colors.brandOn, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
