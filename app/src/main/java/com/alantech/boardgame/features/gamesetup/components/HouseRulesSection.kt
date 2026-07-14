package com.alantech.boardgame.features.gamesetup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alantech.boardgame.R
import com.alantech.boardgame.config.GameSettingConfigCurrentSession
import com.alantech.boardgame.ui.app.LocalSnackbarHostState
import com.alantech.boardgame.utils.PlusJakartaSans
import kotlinx.coroutines.launch

@Composable
fun HouseRulesSection(
    modifier: Modifier = Modifier,
    recordDares: Boolean = true,
    nsfwContent: Boolean = false,
    speedMode: Boolean = false,
    penalty: Boolean = false,
    round: Int = 5,
    onRecordDaresChange: (Boolean) -> Unit = {},
    onNsfwContentChange: (Boolean) -> Unit = {},
    onSpeedModeChange: (Boolean) -> Unit = {},
    onPenaltyChange: (Boolean) -> Unit = {},
    onRoundChange: (Int) -> Unit = {}
) {

    var penaltyInputString = remember {
        mutableStateOf(GameSettingConfigCurrentSession.penaltyInput)
    }
    val snackBar = LocalSnackbarHostState.current
    val featureIsSoonAvailable = stringResource(R.string.feature_is_soon_available)
    val scopedCoroutine = rememberCoroutineScope ()

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
            onCheckedChange = {
                scopedCoroutine.launch {
                    snackBar.showSnackbar(
                        message = featureIsSoonAvailable,
                        duration = SnackbarDuration.Short
                    )
                }
//                onRecordDaresChange(it)
            },
            isAvailable = false
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
            onCheckedChange = { onNsfwContentChange(it) }
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
            onCheckedChange = { onSpeedModeChange(it) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        RuleEditableItem(
            title = "Penalty",
            description = if(penalty) "Skipping a turn, do the penalty" else "No penalty",
            iconColor = Color(0xFFB975FF),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color(0xFF3F007D)
                )
            },
            isChecked = penalty,
            onCheckedChange = { onPenaltyChange(it) },
            inputText = penaltyInputString.value,
            onInputTextChange = {
                penaltyInputString.value = it
                GameSettingConfigCurrentSession.penaltyInput = it

            }
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
            onProgressChange = { onRoundChange(it) }
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
    isAvailable: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF332D3B), RoundedCornerShape(32.dp))
            .alpha(
                if(isAvailable) 1f else 0.5f
            )
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

@Composable
fun RuleEditableItem(
    title: String,
    description: String,
    iconColor: Color,
    icon: @Composable () -> Unit,
    isChecked: Boolean,
    inputText: String,
    onCheckedChange: (Boolean) -> Unit,
    onInputTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF332D3B), RoundedCornerShape(32.dp))
            .padding(8.dp)
            .padding(end = 8.dp)
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

        AnimatedVisibility(
            visible = isChecked,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB975FF),
                    unfocusedBorderColor = Color(0xFF4A4453),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFB975FF)
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
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

