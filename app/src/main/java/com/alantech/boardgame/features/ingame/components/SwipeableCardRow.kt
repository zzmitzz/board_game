package com.alantech.boardgame.features.ingame.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.alantech.boardgame.ui.model.CardDetail
import kotlinx.coroutines.launch

private const val STACK_VISIBLE_CARDS = 3
private const val STACK_OFFSET_DP = 10f
private const val STACK_SCALE_STEP = 0.04f

private const val FLY_OFF_DURATION_MS = 320
private const val FLY_OFF_ROTATION_DEG = -18f

@Composable
fun TinderCardStack(
    modifier: Modifier = Modifier,
    cards: List<CardDetail>,
    currentIndex: Int,
    penalty: String,
    onCardHintClick: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    val flyOffX = remember { Animatable(0f) }
    val flyOffAlpha = remember { Animatable(1f) }
    val flyOffRotation = remember { Animatable(0f) }
    val newCardScale = remember { Animatable(1f) }
    val newCardAlpha = remember { Animatable(1f) }

    // -1 means no card is actively animating in/out — uses plain parallax
    var departingIndex by remember { mutableIntStateOf(-1) }
    var arrivingIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(currentIndex) {
        if (currentIndex == 0 && departingIndex == -1) return@LaunchedEffect
        val anchorIndex = maxOf(0, currentIndex)
        departingIndex = currentIndex - 1
        arrivingIndex = currentIndex

        flyOffX.snapTo(0f)
        flyOffAlpha.snapTo(1f)
        flyOffRotation.snapTo(0f)
        newCardScale.snapTo(0.88f)
        newCardAlpha.snapTo(0f)

        launch {
            launch {
                flyOffRotation.animateTo(
                    targetValue = FLY_OFF_ROTATION_DEG,
                    animationSpec = tween(durationMillis = FLY_OFF_DURATION_MS)
                )
            }
            launch {
                flyOffX.animateTo(
                    targetValue = -1.2f,
                    animationSpec = tween(durationMillis = FLY_OFF_DURATION_MS)
                )
            }
            flyOffAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = FLY_OFF_DURATION_MS - 60)
            )
        }

        launch {
            launch {
                newCardScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            newCardAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 200)
            )
        }

        lazyListState.animateScrollToItem(anchorIndex)

        departingIndex = -1
        arrivingIndex = -1
    }

    val scrollFraction by remember {
        derivedStateOf {
            val itemSize = lazyListState.layoutInfo.visibleItemsInfo
                .firstOrNull()?.size?.takeIf { it > 0 }
                ?: return@derivedStateOf lazyListState.firstVisibleItemIndex.toFloat()
            lazyListState.firstVisibleItemIndex +
                    lazyListState.firstVisibleItemScrollOffset.toFloat() / itemSize
        }
    }

    LazyRow(
        state = lazyListState,
        userScrollEnabled = false,
        modifier = modifier,
    ) {
        itemsIndexed(items = cards, key = { _, card -> card.id }) { index, card ->
            val isDeparting = index == departingIndex
            val isArriving = index == arrivingIndex

            CardEffectWrapper(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .fillParentMaxHeight()
                    .zIndex((cards.size - index).toFloat())
                    .graphicsLayer {
                        val relativePos = index - scrollFraction
                        val baseTranslationX = -size.width * relativePos
                        val baseTranslationY = relativePos.coerceAtLeast(0f) * STACK_OFFSET_DP
                        val baseScale = 1f - relativePos.coerceIn(
                            0f, STACK_VISIBLE_CARDS.toFloat()
                        ) * STACK_SCALE_STEP
                        val baseAlpha = (1f + relativePos).coerceIn(0f, 1f)

                        when {
                            isDeparting -> {
                                translationX = baseTranslationX + size.width * flyOffX.value
                                translationY = baseTranslationY
                                scaleX = baseScale
                                scaleY = baseScale
                                rotationZ = flyOffRotation.value
                                alpha = (baseAlpha * flyOffAlpha.value).coerceIn(0f, 1f)
                            }
                            isArriving -> {
                                translationX = baseTranslationX
                                translationY = baseTranslationY
                                scaleX = baseScale * newCardScale.value
                                scaleY = baseScale * newCardScale.value
                                rotationZ = 0f
                                alpha = (baseAlpha * newCardAlpha.value).coerceIn(0f, 1f)
                            }
                            else -> {
                                translationX = baseTranslationX
                                translationY = baseTranslationY
                                scaleX = baseScale
                                scaleY = baseScale
                                rotationZ = 0f
                                alpha = baseAlpha
                            }
                        }
                    },
                item = card,
                penalty = penalty,
                onCardHintClick = if (index == currentIndex) onCardHintClick else ({}),
            )
        }
    }
}

@Composable
fun CardEffectWrapper(
    modifier: Modifier,
    item: CardDetail,
    penalty: String,
    onCardHintClick: () -> Unit
) {
    ChallengeCard(
        category = item.category,
        challengeText = item.description,
        penaltyText = penalty,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        onCardHintClick = onCardHintClick
    )
}