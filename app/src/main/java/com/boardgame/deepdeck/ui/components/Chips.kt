package com.boardgame.deepdeck.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Motion

/**
 * Streak chip: 🔥 + count. Pops (1 → 1.25 → 1) whenever [count] increases.
 */
@Composable
fun StreakChip(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableIntStateOf(count) }
    val reduced = DeepTalkTheme.reducedMotion
    LaunchedEffect(count) {
        if (count > previous && !reduced) {
            scale.animateTo(1.25f, tween(140, easing = Motion.EmphasizedDecelerate))
            scale.animateTo(1f, Motion.popSpring())
        }
        previous = count
    }
    val active = count > 0
    val description = pluralStringResource(R.plurals.streak_days, count, count)
    Row(
        modifier = modifier
            .height(36.dp)
            .then(if (active) Modifier.glow(colors.ember, radius = 14.dp, alpha = 0.25f, offsetY = 0.dp) else Modifier)
            .clip(DeepTalkShapes.pill)
            .background(colors.glass)
            .border(
                1.dp,
                if (active) colors.ember.copy(alpha = 0.45f) else colors.outline,
                DeepTalkShapes.pill
            )
            .pressable(onClick = onClick, onClickLabel = description)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.LocalFireDepartment,
            contentDescription = description,
            tint = if (active) colors.ember else colors.textMuted,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = count.toString(),
            style = DeepTalkTheme.type.label,
            color = if (active) colors.gold else colors.textSecondary
        )
    }
}

/**
 * Segmented control with a sliding brand indicator (Library tab, filters).
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(DeepTalkShapes.pill)
            .background(colors.glass)
            .border(1.dp, colors.outline, DeepTalkShapes.pill)
            .padding(4.dp)
    ) {
        if (options.isEmpty()) return@BoxWithConstraints
        val segmentWidth = maxWidth / options.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = Motion.pressSpring(),
            label = "segmentIndicator"
        )
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(DeepTalkShapes.pill)
                .background(Brush.linearGradient(listOf(colors.brand, colors.brandStrong)))
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.Start) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .clip(DeepTalkShapes.pill)
                        .pressable(role = Role.Tab, pressedScale = 0.97f) { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = DeepTalkTheme.type.label,
                        color = if (selected) colors.brandOn else colors.textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Section title row with optional trailing action ("See all"). */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = DeepTalkTheme.colors
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(title, style = DeepTalkTheme.type.headline, color = colors.textPrimary)
            subtitle?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = DeepTalkTheme.type.body, color = colors.textSecondary)
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = DeepTalkTheme.type.label,
                color = colors.brand,
                modifier = Modifier
                    .clip(DeepTalkShapes.pill)
                    .pressable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}
