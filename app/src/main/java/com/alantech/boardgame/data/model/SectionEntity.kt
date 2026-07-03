package com.alantech.boardgame.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class SectionEntity (
    @SerialName("id"            ) var id           : String?  = null,
    @SerialName("name"          ) var name         : String?  = null,
    @SerialName("description"   ) var description  : String?  = null,
    @SerialName("ui_type"       ) var uiType       : String?  = null,
    @SerialName("display_order" ) var displayOrder : Int?     = null,
    @SerialName("is_active"     ) var isActive     : Boolean? = null,
    @SerialName("created_at"    ) var createdAt    : String?  = null
)
