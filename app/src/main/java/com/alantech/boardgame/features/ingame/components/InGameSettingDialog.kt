package com.alantech.boardgame.features.ingame.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.theme.LightDialogBackground
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.utils.PlusJakartaSans


@Composable
fun InGameSettingDialog(
    hapticEnabled: Boolean = false,
    soundEnabled: Boolean = false,
    onSave: (Boolean, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var isHapticsEnabled by remember { mutableStateOf(hapticEnabled) }
    var isSoundEnabled by remember { mutableStateOf(soundEnabled) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(color = LightDialogBackground, RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Vibration,
                tint = LightTextColor,
                contentDescription = stringResource(R.string.enable_haptics)
            )
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
            Text(
                text = stringResource(R.string.enable_haptics),
                color = LightTextColor,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
            Switch(
                checked = isHapticsEnabled,
                onCheckedChange = { isHapticsEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF3F007D),
                    checkedTrackColor = Color(0xFFE8D4FF),
                    uncheckedThumbColor = Color(0xFFA19AA8),
                    uncheckedTrackColor = Color(0xFF4A4453)
                )
            )
        }
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Audiotrack,
                tint = LightTextColor,
                contentDescription = stringResource(R.string.enable_haptics)
            )
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
            Text(
                text = stringResource(R.string.enable_sound),
                color = LightTextColor,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
            Switch(
                checked = isSoundEnabled,
                onCheckedChange = { isSoundEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF3F007D),
                    checkedTrackColor = Color(0xFFE8D4FF),
                    uncheckedThumbColor = Color(0xFFA19AA8),
                    uncheckedTrackColor = Color(0xFF4A4453)
                )
            )
        }
        Spacer(
            modifier = Modifier
                .height(16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "Cancel", // Replace with stringResource(R.string.cancel) if available
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Button(
                onClick = {
                    // Constructs a copy of the setting with the freshly mutated state variables
                    onSave(isHapticsEnabled, isSoundEnabled)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F007D) // Color matching your active Switch thumb
                )
            ) {
                Text(
                    text = "Save", // Replace with stringResource(R.string.save) if available
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}