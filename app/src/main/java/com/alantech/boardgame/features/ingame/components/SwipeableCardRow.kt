package com.alantech.boardgame.features.ingame.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.ui.model.CardDetail
import com.alantech.boardgame.ui.model.cardDetailPack

@Composable
fun CardEffectWrapper(
    modifier : Modifier,
    item: CardDetail,
    penalty: String,
    onCardHintClick: () -> Unit
) {
    ChallengeCard(
        category = item.category,
        challengeText = item.description,
        penaltyText = penalty,
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 24.dp),
        onCardHintClick = onCardHintClick
    )
}