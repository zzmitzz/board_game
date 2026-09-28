package com.boardgame.deepdeck.features.home.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.boardgame.deepdeck.data.model.VibeCategory
import com.boardgame.deepdeck.ui.theme.VibeGradients
import com.boardgame.deepdeck.ui.theme.toComposeColorOrNull

/** "Who are you with?" tile model. Colors default per icon key, overridable from admin. */
@Immutable
data class VibeUi(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorStart: Color,
    val colorEnd: Color,
    val packCount: Int?,
    val description: String?,
)

/** Resolves an icon key from the API or, for old backends, from the English name. */
fun resolveVibeIconKey(iconKey: String?, name: String?): String {
    iconKey?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }?.let { return it }
    val n = name.orEmpty().lowercase()
    return when {
        "friend" in n -> "friends"
        "party" in n -> "party"
        "drink" in n || "beer" in n || "bar" in n -> "drinking"
        "couple" in n || "date" in n -> "couple"
        "love" in n || "romance" in n -> "love"
        "family" in n -> "family"
        "work" in n || "team" in n || "office" in n -> "work"
        "solo" in n || "self" in n -> "solo"
        else -> "party"
    }
}

fun defaultVibeGradient(iconKey: String): Pair<Color, Color> = when (iconKey) {
    "friends", "family", "work" -> VibeGradients.Friends
    "party" -> VibeGradients.Party
    "drinking" -> VibeGradients.Drinking
    "love", "couple" -> VibeGradients.Love
    else -> VibeGradients.Fallback
}

fun VibeCategory.toVibeUi(): VibeUi? {
    val vibeId = id ?: return null
    // A vibe without packs is a dead end (older cached feeds can still contain one).
    if (packCount == 0) return null
    val displayName = name?.takeIf { it.isNotBlank() } ?: categoryEn.orEmpty()
    val key = resolveVibeIconKey(iconKey, categoryEn ?: name)
    val (defStart, defEnd) = defaultVibeGradient(key)
    return VibeUi(
        id = vibeId,
        name = displayName,
        iconKey = key,
        colorStart = colorStart.toComposeColorOrNull() ?: defStart,
        colorEnd = colorEnd.toComposeColorOrNull() ?: defEnd,
        packCount = packCount,
        description = description?.takeIf { it.isNotBlank() },
    )
}
