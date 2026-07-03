package com.alantech.boardgame.features.pagedetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import java.util.Locale

@Composable
fun SampleCardItem(
    tag: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = (Color("#231E27".toColorInt())),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier
            .width(220.dp)
            .aspectRatio(0.65f) // Similar to a playing card
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tag.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFD0A5FF),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = content,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCFC2D6)
            )

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info, // Placeholder for the small card icon
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview
@Composable
private fun SamplePackDetailUIModel() {
    SampleCardItem(
        tag = "Tag",
        content = "Content"
    )
    
}