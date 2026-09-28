package com.boardgame.deepdeck.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.theme.DeepTalkPalette
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme

/** Server-computed pack badge (§5.3): PREMIUM › NEW › HOT. */
enum class PackBadge(@StringRes val label: Int, val color: Color, val icon: ImageVector) {
    NEW(R.string.badge_new, DeepTalkPalette.Gold, Icons.Rounded.AutoAwesome),
    HOT(R.string.badge_hot, DeepTalkPalette.Ember, Icons.Rounded.LocalFireDepartment),
    PREMIUM(R.string.badge_premium, DeepTalkPalette.Brand, Icons.Rounded.Diamond);

    companion object {
        fun from(raw: String?): PackBadge? =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) }
    }
}

/** Small tinted pill: NEW (gold) · HOT (ember) · PREMIUM (brand). */
@Composable
fun PackBadgeChip(
    badge: PackBadge,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
) {
    val bg = if (solid) badge.color else DeepTalkTheme.colors.bgBase.copy(alpha = 0.72f)
    val fg = if (solid) DeepTalkTheme.colors.brandOn else badge.color
    Row(
        modifier = modifier
            .background(bg, DeepTalkShapes.pill)
            .border(1.dp, badge.color.copy(alpha = if (solid) 0f else 0.45f), DeepTalkShapes.pill)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(badge.icon, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        Text(
            text = stringResource(badge.label).uppercase(),
            style = DeepTalkTheme.type.overline,
            color = fg
        )
    }
}

/** Generic overline pill (levels, categories). */
@Composable
fun OverlinePill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.16f), DeepTalkShapes.pill)
            .border(1.dp, color.copy(alpha = 0.35f), DeepTalkShapes.pill)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(12.dp)) }
        Text(text = text.uppercase(), style = DeepTalkTheme.type.overline, color = color)
    }
}
