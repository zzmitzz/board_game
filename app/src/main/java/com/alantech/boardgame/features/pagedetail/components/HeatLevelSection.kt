package com.alantech.boardgame.features.pagedetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.alantech.boardgame.R

@Composable
fun HeatLevelSection(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = (Color("#231E27".toColorInt())),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info, // Placeholder for thermometer
                        contentDescription = null,
                        tint = Color(0xFFD0A5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Heat Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEADFED)
                    )
                }
                Text(
                    text = "EXTREME",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFD0A5FF),
                    fontWeight = FontWeight.Bold
                )
            }

            // Gradient Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFD0A5FF).copy(alpha = 0.5f),
                                Color(0xFFD0A5FF),
                                MaterialTheme.colorScheme.surfaceVariant
                            ),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY
                        ),
                        shape = CircleShape
                    )
            ) {
                // Indicator thumb (simulated)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 32.dp) // Offset slightly from end
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(4.dp, Color(0xFFD0A5FF), CircleShape)
                )
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.chill),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCFC2D6)
                )
                Text(
                    stringResource(R.string.spicy),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCFC2D6)
                )
                Text(
                    stringResource(R.string.extreme),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFCFC2D6)
                )
            }
        }
    }
}

@Preview
@Composable
private fun HeatPreview() {
    HeatLevelSection()
}