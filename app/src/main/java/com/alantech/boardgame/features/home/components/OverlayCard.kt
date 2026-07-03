package com.alantech.boardgame.features.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.model.PackDetailUIModel
import com.alantech.boardgame.ui.theme.LightTextOnBackground


@Composable
fun OverlayCard(
    card: PackDetailUIModel,
    onCardClick: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .width(160.dp)
            .clickable {
                onCardClick(card.id)
            }
    ){
        AsyncImage(
            model = card.coverImageUrl,
            error = painterResource(R.drawable.image_error),
            contentDescription = "Card Image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.BottomStart)
        ) {
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
}

@Preview
@Composable
private fun prevOverlayCard() {

}