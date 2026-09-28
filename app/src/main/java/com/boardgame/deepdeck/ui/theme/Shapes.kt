package com.boardgame.deepdeck.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Radii: xs 8 · sm 12 · md 16 · lg 24 · xl 32 · pill 999 (plan §2.3). Question card = xl. */
object DeepTalkShapes {
    val xs = RoundedCornerShape(8.dp)
    val sm = RoundedCornerShape(12.dp)
    val md = RoundedCornerShape(16.dp)
    val lg = RoundedCornerShape(24.dp)
    val xl = RoundedCornerShape(32.dp)
    val pill = RoundedCornerShape(999.dp)
}

val MaterialShapes = Shapes(
    extraSmall = DeepTalkShapes.xs,
    small = DeepTalkShapes.sm,
    medium = DeepTalkShapes.md,
    large = DeepTalkShapes.lg,
    extraLarge = DeepTalkShapes.xl,
)
