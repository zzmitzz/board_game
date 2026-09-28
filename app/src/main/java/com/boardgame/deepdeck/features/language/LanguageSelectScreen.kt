package com.boardgame.deepdeck.features.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.boardgame.deepdeck.BoardGameApplication
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import com.boardgame.deepdeck.utils.LanguageItem
import com.boardgame.deepdeck.utils.LocaleUtils
import com.boardgame.deepdeck.utils.findActivity
import com.boardgame.deepdeck.utils.listLanguageSupport

@Composable
fun LanguageSelectScreen(
    vm: LanguageSelectVM,
    onBackClick: () -> Unit = {},
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = DeepTalkTheme.colors

    GlowBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Spacing.gutter, end = Spacing.gutter, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item(key = "top") {
                Column(Modifier.statusBarsPadding().padding(top = Spacing.xs)) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        onClick = onBackClick
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Text(stringResource(R.string.language), style = DeepTalkTheme.type.hero, color = colors.textPrimary)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        stringResource(R.string.language_subtitle),
                        style = DeepTalkTheme.type.body,
                        color = colors.textSecondary
                    )
                }
            }
            item(key = "translate") {
                GlassSurface(Modifier.fillMaxWidth(), tint = colors.brand) {
                    Row(
                        Modifier
                            .pressable(pressedScale = 0.98f, role = Role.Switch) {
                                vm.setTranslateCards(!state.translateCards)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Translate, null, tint = colors.brand)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.translate_cards_title),
                                style = DeepTalkTheme.type.title,
                                color = colors.textPrimary
                            )
                            Text(
                                stringResource(R.string.translate_cards_desc),
                                style = DeepTalkTheme.type.label,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = state.translateCards,
                            onCheckedChange = vm::setTranslateCards,
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
            }
            items(listLanguageSupport, key = { it.code }) { item ->
                LanguageRow(item, item.code == state.selected.code) { vm.select(item) }
            }
        }

        AnimatedVisibility(
            visible = state.hasChanges,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(Spacing.gutter)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.save),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        vm.save { localeChanged, code ->
                            if (localeChanged) {
                                context.findActivity()?.let { LocaleUtils.applyLocaleAndRecreate(it, code) }
                                    ?: (context.applicationContext as BoardGameApplication).applyStoredLocale()
                            } else {
                                onBackClick()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(item: LanguageItem, isSelected: Boolean, onClick: () -> Unit) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DeepTalkShapes.md)
            .background(if (isSelected) colors.brand.copy(alpha = 0.14f) else colors.glass)
            .border(1.dp, if (isSelected) colors.brand.copy(alpha = 0.5f) else colors.outline, DeepTalkShapes.md)
            .pressable(pressedScale = 0.98f, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(item.flagRes),
            contentDescription = null,
            modifier = Modifier.width(32.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = item.fullName,
            style = DeepTalkTheme.type.title.copy(fontSize = DeepTalkTheme.type.body.fontSize),
            color = if (isSelected) colors.textPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(
                Modifier
                    .size(24.dp)
                    .background(colors.brand, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, null, tint = colors.brandOn, modifier = Modifier.size(16.dp))
            }
        }
    }
}
