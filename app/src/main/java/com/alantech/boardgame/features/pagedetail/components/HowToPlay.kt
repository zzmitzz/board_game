package com.alantech.boardgame.features.pagedetail.components

import androidx.collection.intSetOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.model.exampleText

@Composable
fun HowToPlaySection(
    modifier: Modifier = Modifier,
    instruction: String
) {
    val isExpanded = remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color("#231E27".toColorInt()),
        border = BorderStroke(1.dp, color = Color.White.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                isExpanded.value = !isExpanded.value
            }
    ) {
        Column(
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.QuestionMark,
                    contentDescription = null,
                    tint = Color(0xFFD0A5FF)
                )
                Text(
                    text = stringResource(R.string.how_to_play),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.rotate(
                        degrees = if (isExpanded.value) 180f else 0f
                    )
                )
            }
            AnimatedVisibility(
                visible = isExpanded.value
            ) {
                Text(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    textAlign = TextAlign.Start,
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Preview
@Composable
private fun HTPPreview() {
    HowToPlaySection(instruction =  exampleText)
}