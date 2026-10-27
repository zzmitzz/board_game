package com.boardgame.deepdeck.data.model

import com.boardgame.deepdeck.ui.model.CardAssetKind
import com.boardgame.deepdeck.ui.model.backMedia
import com.boardgame.deepdeck.ui.model.frontMedia
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteCardAssetsTest {
    // Same configuration as NetworkModule.provideJson().
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val payload = """
        {
          "id": "c1", "type": "DOUBLE_SIDED", "level": "DEEP", "locale": "en",
          "front_side": null, "back_side": "Back", "hint": null, "is_active": true,
          "assets": [
            {"id": "a3", "kind": "AUDIO", "placement": "FRONT", "role": "INLINE",
             "url": "https://res.cloudinary.com/x/video/upload/a.mp3", "display_order": 2, "is_active": true},
            {"id": "a1", "kind": "IMAGE", "placement": "FRONT", "role": "INLINE",
             "url": "https://res.cloudinary.com/x/image/upload/i.jpg", "display_order": 0, "is_active": true,
             "thumbnail_url": null, "provider": "cloudinary", "extra": {}},
            {"id": "a2", "kind": "VIDEO", "placement": "FRONT", "role": "BACKGROUND",
             "url": "https://res.cloudinary.com/x/video/upload/v.mp4", "display_order": 1, "is_active": true,
             "thumbnail_url": "https://res.cloudinary.com/x/video/upload/v.jpg"},
            {"id": "a4", "kind": "EMBED", "placement": "BACK", "role": "INLINE",
             "url": "https://www.youtube.com/embed/dQw4w9WgXcQ", "display_order": 0, "is_active": true},
            {"id": "a5", "kind": "IMAGE", "placement": "FRONT", "role": "INLINE",
             "url": "https://res.cloudinary.com/x/image/upload/off.jpg", "display_order": 3, "is_active": false},
            {"id": "a6", "kind": "HOLOGRAM", "placement": "FRONT", "role": "INLINE",
             "url": "https://example.com/h", "display_order": 4, "is_active": true}
          ]
        }
    """.trimIndent()

    @Test
    fun `assets survive parsing and are split per side`() {
        val card = json.decodeFromString(RemoteCard.serializer(), payload).toUIModel()

        // Inactive (a5) and unknown kinds (a6) are dropped; display_order is per side.
        assertEquals(listOf("a1", "a2", "a3", "a4"), card.assets.map { it.id }.sorted())
        assertEquals("https://res.cloudinary.com/x/image/upload/i.jpg", card.media.image)
        assertEquals("https://res.cloudinary.com/x/video/upload/v.mp4", card.media.video)

        val front = card.frontMedia()
        assertEquals("a2", front.background?.id)
        assertEquals(CardAssetKind.VIDEO, front.background?.kind)
        assertEquals(listOf("a1", "a3"), front.inline.map { it.id })

        val back = card.backMedia()
        assertNull(back.background)
        assertEquals(listOf("a4"), back.inline.map { it.id })
    }

    @Test
    fun `cards without assets still parse`() {
        val card = json.decodeFromString(RemoteCard.serializer(), """{"id": "c2", "front_side": "Hi"}""").toUIModel()

        assertEquals(emptyList<Any>(), card.assets)
        assertEquals(true, card.frontMedia().isEmpty)
    }
}
