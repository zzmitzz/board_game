package com.alantech.boardgame.features.pagedetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.utils.addBgGradient


@Composable
fun CardBadge(
    badgeText: String
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF4A148C).copy(alpha = 0.3f), // Glow/Brand accent
        border = BorderStroke(1.dp, Color(0xFFD0A5FF).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info, // Placeholder for fire/trending icon
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFFD0A5FF)
            )
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFD0A5FF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun ThumbnailSection(
    modifier: Modifier,
    size: Dp,
    imageUrl: String? = null
) {
    Box(
        modifier
            .size(size)
    ) {
        AsyncImage(
            model = imageUrl,
            modifier = Modifier
                .fillMaxSize()
                .blur(12.dp),
            error = painterResource(R.drawable.image_error),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .addBgGradient(
                    Color.Transparent,
                    LightBackground
                )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Preview1() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        CardBadge("Trending")
        ThumbnailSection(Modifier, 500.dp, imageUrl = null)
    }
}
