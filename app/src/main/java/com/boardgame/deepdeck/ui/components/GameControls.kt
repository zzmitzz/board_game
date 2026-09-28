package com.boardgame.deepdeck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boardgame.deepdeck.ui.model.GamePlayer
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme

/** Player avatar: colored disc with the player's emoji (or initial). */
@Composable
fun PlayerAvatar(
    player: GamePlayer,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    ring: Color? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(player.color.copy(alpha = 0.9f))
            .then(if (ring != null) Modifier.border(2.dp, ring, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = player.avatar,
            fontSize = (size.value * 0.5f).sp,
            color = DeepTalkTheme.colors.brandOn,
            textAlign = TextAlign.Center,
        )
    }
}

/** Icon + title/description + switch; the whole row toggles (accessible). */
@Composable
fun ToggleRow(
    icon: ImageVector,
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = DeepTalkTheme.colors.brand,
    enabled: Boolean = true,
) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(DeepTalkShapes.sm)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = DeepTalkTheme.type.title, color = colors.textPrimary)
            description?.let { Text(it, style = DeepTalkTheme.type.label, color = colors.textSecondary) }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.brandOn,
                checkedTrackColor = accent,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceHigh,
                uncheckedBorderColor = colors.outline,
            )
        )
    }
}

/** − value + stepper with accessible labels. */
@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    decrementLabel: String,
    incrementLabel: String,
    modifier: Modifier = Modifier,
    valueDescription: String = value.toString(),
) {
    val colors = DeepTalkTheme.colors
    Row(
        modifier = modifier.semantics { stateDescription = valueDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepperButton(Icons.Rounded.Remove, decrementLabel, enabled = value > range.first) { onValueChange(value - 1) }
        Text(
            text = value.toString(),
            style = DeepTalkTheme.type.headline,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp)
        )
        StepperButton(Icons.Rounded.Add, incrementLabel, enabled = value < range.last) { onValueChange(value + 1) }
    }
}

@Composable
private fun StepperButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = DeepTalkTheme.colors
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (enabled) colors.surfaceHigh else colors.surface)
            .border(1.dp, colors.outline, CircleShape)
            .pressable(enabled = enabled, pressedScale = 0.9f, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) colors.textPrimary else colors.textMuted)
    }
}
