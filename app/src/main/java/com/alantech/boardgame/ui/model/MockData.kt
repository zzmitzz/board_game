package com.alantech.boardgame.ui.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


val dataCardThumb = listOf<CardPreview>(
    CardPreview(
        id = "1",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    CardPreview(
        id = "2",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    CardPreview(
        id = "3",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    CardPreview(
        id = "4",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
    CardPreview(
        id = "5",
        thumbnail = "https://play-lh.googleusercontent.com/6y8IP2DxJl3d9avDZTG3tZSssk9m26akjMjuv-k5-tScdzNAqjwodmNPFns02DAaBNc=w480-h960-rw",
        titleCard = "Card 1",
        creator = "Creator 1"
    ),
)

val exampleText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

suspend fun loadingCardsDetailPack(): List<CardDetail> {
    withContext(Dispatchers.IO){
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
                    )

                )
            )
        }
    }
}