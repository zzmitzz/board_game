package com.alantech.boardgame.features.gamesetup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.ui.tooling.preview.Preview
import com.alantech.boardgame.utils.PlusJakartaSans

@Composable
fun HouseRulesSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(
        bottom = 16.dp
    )) {
        Text(
            text = "House Rules",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        var recordDares by remember { mutableStateOf(true) }
        var nsfwContent by remember { mutableStateOf(false) }
        var speedMode by remember { mutableStateOf(false) }
        var penalty by remember { mutableStateOf(false) }
        var round by remember { mutableIntStateOf(5) }

        RuleItem(
            title = "Record Dares",
            description = "Capture the best moments",
            iconColor = Color(0xFFB975FF),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Videocam,
                    contentDescription = null,
                    tint = Color(0xFF3F007D)
                )
            },
            isChecked = recordDares,
            onCheckedChange = { recordDares = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RuleItem(
            title = "NSFW Content",
            description = "Include NSFW cards",
            iconColor = Color(0xFFB975FF),
            icon = {
                Text(
                    text = "18",
                    color = Color(0xFF3F007D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            isChecked = nsfwContent,
            onCheckedChange = { nsfwContent = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        RuleItem(
            title = "Speed Mode",
            description = "30s timer per turn",
            iconColor = Color(0xFFB975FF),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = Color(0xFF3F007D)
                )
            },
            isChecked = speedMode,
            onCheckedChange = { speedMode = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        RuleItem(
            title = "Penalty",
            description = if(penalty) "Skipping a turn, takes 2 shots" else "No penalty",
            iconColor = Color(0xFFB975FF),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color(0xFF3F007D)
                )
            },
            isChecked = penalty,
            onCheckedChange = { penalty = it }
        )


        Spacer(modifier = Modifier.height(16.dp))

        RuleProgressItem(
            title = "Round",
            description = "Number of play rounds",
            iconColor = Color(0xFFB975FF),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Replay,
                    contentDescription = null,
                    tint = Color(0xFF3F007D)
                )
            },
            progress = round,
            onProgressChange = { round = it }
        )
    }
}

@Composable
fun RuleItem(
    title: String,
    description: String,
    iconColor: Color,
    icon: @Composable () -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF332D3B), RoundedCornerShape(32.dp))
            .padding(8.dp)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(iconColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = Color(0xFFA19AA8),
                fontSize = 12.sp
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF3F007D),
                checkedTrackColor = Color(0xFFE8D4FF),
                uncheckedThumbColor = Color(0xFFA19AA8),
                uncheckedTrackColor = Color(0xFF4A4453)
            )
        )
    }
}


@Preview
@Composable
private fun PVHouseRuleSec() {
    HouseRulesSection()
}


@Composable
fun RuleProgressItem(
    title: String,
    description: String,
    iconColor: Color,
    icon: @Composable () -> Unit,
    progress: Int,
    onProgressChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF332D3B), RoundedCornerShape(32.dp))
            .padding(8.dp)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(iconColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        color = Color(0xFFA19AA8),
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = progress.toString(),
                    color = Color(0xFFD9D9D9),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                modifier = Modifier.padding(horizontal = 8.dp),
                value = progress.toFloat(),
                onValueChange = { onProgressChange(it.toInt()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                valueRange = 5f..20f,
                steps = 2
            )
        }
    }
}

