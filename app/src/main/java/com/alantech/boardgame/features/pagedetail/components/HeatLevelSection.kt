package com.alantech.boardgame.features.pagedetail.components

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.alantech.boardgame.R
import com.alantech.boardgame.features.pagedetail.components.HeatLevelEnum.BLAZING
import com.alantech.boardgame.features.pagedetail.components.HeatLevelEnum.HOT
import com.alantech.boardgame.features.pagedetail.components.HeatLevelEnum.MILD
import com.alantech.boardgame.features.pagedetail.components.HeatLevelEnum.SPICY
import com.alantech.boardgame.features.pagedetail.components.HeatLevelEnum.WARM


enum class HeatLevelEnum (@StringRes val label: Int) {
    MILD(R.string.mild),
    WARM(R.string.warm),
    HOT(R.string.hot),
    SPICY(R.string.spicy),
    BLAZING(R.string.blazing);

}
private fun getLevelFromFloat(
    level: Int
): HeatLevelEnum {
    Log.i("HeatLevelEnum", "getLevelFromFloat: $level")
    return when (level) {
        1 -> HeatLevelEnum.MILD
        2 -> HeatLevelEnum.WARM
        3 -> HeatLevelEnum.HOT
        4 -> HeatLevelEnum.SPICY
        5 -> HeatLevelEnum.BLAZING
        else -> MILD
    }
}
@Composable
fun HeatLevelSection(
    heatLevel: Int,
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
                        text = stringResource(R.string.heat_level),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEADFED)
                    )
                }
                Text(
                    text = stringResource(getLevelFromFloat(heatLevel).label),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFD0A5FF),
                    fontWeight = FontWeight.Bold
                )
            }

            // Gradient Bar

            Box(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .clip(CircleShape) // Ensures the custom drawing doesn't bleed past the rounded corners
                        .drawWithContent {
                            drawContent()

                            // 2. Calculate the dynamic width of the progress fill
                            val progressWidth = size.width * (heatLevel/5f)

                            // 3. Draw the gradient progress bar up to that specific width
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFD0A5FF).copy(alpha = 0.5f),
                                        Color(0xFFD0A5FF),
                                    ),
                                    startX = 0f,
                                    endX = progressWidth
                                ),
                                size = size.copy(width = progressWidth) // This restricts the drawn area!,
                            )
                        }
                )
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeatLevelEnum.entries.forEach {
                    Text(
                        stringResource(it.label),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCFC2D6)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HeatPreview() {
    HeatLevelSection(heatLevel = 1)
}