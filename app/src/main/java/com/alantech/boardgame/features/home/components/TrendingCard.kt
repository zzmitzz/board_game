package com.alantech.boardgame.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.model.PackDetailUIModel
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightTextOnBackground

@Composable
fun TrendingCard(
    card: PackDetailUIModel,
    onCardClick: (String) -> Unit,
    ) {

    Column(
        modifier = Modifier.width(160.dp).clickable {
            onCardClick(card.id)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(LightPrimary, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(16.dp))
        ){
            AsyncImage(
                model = card.thumbnail,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = card.titleCard,
            color = LightTextOnBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = stringResource(R.string.create_by) + "${card.creator}",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
