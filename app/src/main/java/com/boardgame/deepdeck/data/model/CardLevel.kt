package com.boardgame.deepdeck.data.model

/**
 * Card depth levels. The backend emits `INTIMATE`; legacy rows/clients may
 * still send the misspelled `INMATE`, which is normalized here.
 */
enum class CardLevel {
    ICEBREAKER, DEEP, INTIMATE, UNKNOWN;

    companion object {
        fun from(raw: String?): CardLevel = when (raw?.trim()?.uppercase()) {
            "ICEBREAKER", "ICE_BREAKER" -> ICEBREAKER
            "DEEP" -> DEEP
            "INTIMATE", "INMATE" -> INTIMATE
            else -> UNKNOWN
        }
    }
}

/** Normalizes a raw level string (maps INMATE → INTIMATE); returns null for blanks. */
fun normalizeLevel(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val level = CardLevel.from(raw)
    return if (level == CardLevel.UNKNOWN) raw else level.name
}
