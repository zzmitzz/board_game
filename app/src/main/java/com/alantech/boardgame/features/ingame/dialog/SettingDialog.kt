package com.alantech.boardgame.features.ingame.dialog

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.alantech.boardgame.config.PersistenceSetting
import com.alantech.boardgame.features.ingame.components.CustomizeSpinner
import com.alantech.boardgame.ui.theme.LightDialogBackground
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.utils.PlusJakartaSans
import com.alantech.boardgame.utils.listLanguageSupport

@Composable
fun SettingDialog(
    initialSetting: PersistenceSetting,
    onSave: (PersistenceSetting) -> Unit,
    onCancel: () -> Unit
) {
    var isAutoTranslate by remember { mutableStateOf(initialSetting.isAutoTranslate) }
    var currLanguage by remember { mutableStateOf(
        listLanguageSupport.find { it.code.equals(initialSetting.language, ignoreCase = true) } ?: listLanguageSupport[0]
    ) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(color = LightDialogBackground, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Auto Translate Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Swapped to SpaceBetween for better alignment with spinners
        ) {
            Text(
                text = stringResource(R.string.auto_translate),
                color = LightTextColor,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = isAutoTranslate,
                onCheckedChange = { isAutoTranslate = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF3F007D),
                    checkedTrackColor = Color(0xFFE8D4FF),
                    uncheckedThumbColor = Color(0xFFA19AA8),
                    uncheckedTrackColor = Color(0xFF4A4453)
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Language Selection Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.choose_language),
                color = if (isAutoTranslate) LightTextColor else LightTextColor.copy(alpha = 0.4f),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            CustomizeSpinner(
                options = listLanguageSupport,
                selectedOption = currLanguage.fullName,
                onOptionSelected = { language ->
                    currLanguage = language
                },
                label = stringResource(R.string.choose_language),
                enabled = isAutoTranslate
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Action Buttons Row (Exit/Save) ---
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
                    onSave(
                        initialSetting.copy(
                            isAutoTranslate = isAutoTranslate,
                            language = currLanguage.code
                        )
                    )
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