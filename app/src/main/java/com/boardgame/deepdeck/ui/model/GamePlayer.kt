package com.boardgame.deepdeck.ui.model

import androidx.compose.ui.graphics.Color
import com.boardgame.deepdeck.utils.serializable.CustomColorSerialization
import kotlinx.serialization.Serializable

/**
 * A player in the roster. [id] is stable for the whole roster lifetime (never reused after a
 * delete); it stays an Int so game history rows stored before the upgrade still decode.
 * [emoji] is the avatar glyph (blank on legacy rows → the initial is shown instead).
 */
@Serializable
data class GamePlayer(
    val id: Int,
    @Serializable(with = CustomColorSerialization::class)
    val color: Color,
    val name: String,
    val emoji: String = "",
) {
    /** Avatar glyph: emoji when set, else the first letter of the name. */
    val avatar: String
        get() = emoji.ifBlank { name.trim().take(1).uppercase().ifBlank { "?" } }
}

/** Curated avatar palette (readable on the Midnight Velvet background). */
object PlayerPalette {
    val colors = listOf(
        Color(0xFFB57BFF), Color(0xFF5EE6B0), Color(0xFFFF5C8A), Color(0xFFFFC76B),
        Color(0xFF6FC3FF), Color(0xFFFF7A59), Color(0xFF9B6BFF), Color(0xFF4DD4E8),
        Color(0xFFF78FD1), Color(0xFFA3E36B),
    )
    val emojis = listOf(
        "🦊", "🐼", "🦄", "🐙", "🐯", "🐸", "🦉", "🐳", "🐝", "🦋",
        "🌵", "🍉", "🌙", "⭐", "🔥", "🍀", "🎧", "🎲", "🍕", "🚀",
    )

    fun colorFor(index: Int): Color = colors[Math.floorMod(index, colors.size)]
    fun emojiFor(index: Int): String = emojis[Math.floorMod(index, emojis.size)]
}
