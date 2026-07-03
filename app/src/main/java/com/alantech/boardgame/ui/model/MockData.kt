package com.alantech.boardgame.ui.model

import com.alantech.boardgame.R
import com.alantech.boardgame.features.home.model.VibeChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


val dataCardThumb = listOf<PackDetailUIModel>(
    PackDetailUIModel(
        id = "1",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    PackDetailUIModel(
        id = "2",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    PackDetailUIModel(
        id = "3",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    PackDetailUIModel(
        id = "4",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    PackDetailUIModel(
        id = "5",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
)

val exampleText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

suspend fun loadingCardsDetailPack(): List<CardDetail> {
    return withContext(Dispatchers.IO){
        delay(2000)
        val results = mutableListOf<CardDetail>()
        repeat(40){
            results.add(
                CardDetail(
                    id = it.toString(),
                    category = "Dare",
                    description = "There is the ${it} card-th",
                    media = CardDetailMedia(
                        image = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
                        video = null
                    ), hint = ""

                )
            )
        }
        return@withContext results
    }
}



val cardDetailPack = mutableListOf<CardDetail>().also { listData ->
    repeat(40){
        listData.add(
            CardDetail(
                id = it.toString(),
                category = "Dare",
                description = "There is the ${it} card-th",
                media = CardDetailMedia(
                    image = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
                    video = null
                ),
                hint = ""

            )
        )
    }
}

val mockVibeChip = listOf<VibeChip>(
    VibeChip(
        id = "1",
        name = "Party",
        icon = R.drawable.ic_party
    ),
    VibeChip(
        id = "2",
        name = "Friend",
        icon = R.drawable.ic_friend
    ),
    VibeChip(
        id = "3",
        name = "Drinking",
        icon = R.drawable.ic_beer
    ),
    VibeChip(
        id = "4",
        name = "Love",
        icon =  R.drawable.ic_love
    )
)