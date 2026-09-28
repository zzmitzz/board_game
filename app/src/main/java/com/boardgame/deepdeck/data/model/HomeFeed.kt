package com.boardgame.deepdeck.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /api/v1/home (§5.3). */
@Serializable
data class HomeFeedResponse(
    @SerialName("daily_card") val dailyCard: DailyCardResponse? = null,
    @SerialName("vibes") val vibes: List<VibeCategory> = emptyList(),
    @SerialName("sections") val sections: List<HomeSectionDto> = emptyList(),
)

@Serializable
data class HomeSectionDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("ui_type") val uiType: String? = null,
    @SerialName("display_order") val displayOrder: Int? = null,
    @SerialName("packs") val packs: List<PacksPreview> = emptyList(),
)

fun HomeSectionDto.toSectionEntity() = SectionEntity(
    id = id, name = name, description = description, uiType = uiType, displayOrder = displayOrder
)

/** GET /api/v1/daily-card (§5.3). */
@Serializable
data class DailyCardResponse(
    @SerialName("date") val date: String? = null,
    @SerialName("card") val card: RemoteCard? = null,
    @SerialName("pack") val pack: PacksPreview? = null,
)

/** POST /api/v1/cards/feedback. reaction = LOVE | SKIP | REPORT. */
@Serializable
data class CardFeedbackRequest(
    @SerialName("device_id") val deviceId: String,
    @SerialName("card_id") val cardId: String,
    @SerialName("pack_id") val packId: String? = null,
    @SerialName("reaction") val reaction: String,
)

/** POST /api/v1/events/sessions. */
@Serializable
data class GameSessionCreate(
    @SerialName("device_id") val deviceId: String,
    @SerialName("pack_id") val packId: String? = null,
    @SerialName("is_custom_pack") val isCustomPack: Boolean = false,
    @SerialName("players_count") val playersCount: Int? = null,
    @SerialName("rounds") val rounds: Int? = null,
    @SerialName("cards_completed") val cardsCompleted: Int? = null,
    @SerialName("cards_forfeited") val cardsForfeited: Int? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("locale") val locale: String? = null,
)

@Serializable
data class IdResponse(@SerialName("id") val id: String? = null)

@Serializable
data class OkResponse(@SerialName("ok") val ok: Boolean? = null)
