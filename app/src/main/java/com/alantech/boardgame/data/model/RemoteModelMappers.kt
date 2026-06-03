package com.alantech.boardgame.data.model

import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.CardDetailMedia
import com.alantech.boardgame.ui.model.CardPreview

fun RemotePack.toUIModel(): CardPreview {
    return CardPreview(
        id = id.orEmpty(),
        thumbnail = thumb ?: coverImageUrl.orEmpty(),
        titleCard = title.orEmpty(),
        creator = tag ?: "Premium",
        description = description,
        coverImageUrl = coverImageUrl,
        thumb = thumb,
        estimateTimePlay = estimateTimePlay,
        suggestNumberPlayers = suggestNumberPlayers,
        tag = tag,
        heatLevel = heatLevel,
        totalCards = totalCards,
        howToPlay = howToPlay
    )
}

fun RemoteCard.toUIModel(): CardDetail {
    return CardDetail(
        id = id.orEmpty(),
        category = level ?: type.orEmpty(),
        description = frontSide.orEmpty(),
        media = CardDetailMedia(
            image = null,
            video = null
        ),
        hint = hint.orEmpty()
    )
}
