package com.alantech.boardgame.features.mylibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.R
import com.alantech.boardgame.data.local.entity.LocalCardEntity
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.ui.theme.LightTextOnBackground
import com.alantech.boardgame.utils.PlusJakartaSans

@Composable
fun PackCardsScreen(
    vm: PackCardsVM,
    onBackClick: () -> Unit,
    onAddCardClick: () -> Unit,
    onEditCardClick: (cardId: String) -> Unit,
    onPlayClick: () -> Unit,
) {
    val cards by vm.cards.collectAsState()
    val pack by vm.pack.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = LightTextOnBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack?.title ?: stringResource(R.string.sample_cards),
                        color = LightTextOnBackground,
                        fontFamily = PlusJakartaSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.pack_cards_count, cards.size),
                        color = LightTextColor.copy(alpha = 0.5f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.sp
                    )
                }
                if (cards.isNotEmpty()) {
                    IconButton(onClick = onPlayClick) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.play),
                            tint = LightSecondTextOBG,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = null,
                            tint = LightSecondTextOBG.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.no_cards_yet),
                            color = LightTextOnBackground,
                            fontFamily = PlusJakartaSans,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.add_first_card_hint),
                            color = LightTextColor.copy(alpha = 0.5f),
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cards, key = { it.id }) { card ->
                        CardRow(
                            card = card,
                            onEdit = { onEditCardClick(card.id) },
                            onDelete = { vm.deleteCard(card) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(88.dp)) }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddCardClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = LightSecondTextOBG,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_card))
        }
    }
}

@Composable
private fun CardRow(card: LocalCardEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightPrimary, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = card.description,
                color = LightTextOnBackground,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.category.ifBlank { "General" },
                color = LightSecondTextOBG,
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp
            )
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = LightSecondTextOBG.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = LightTextColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
