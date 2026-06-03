package com.alantech.boardgame.features.ingame.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.utils.PlusJakartaSans

private val SpinnerPurple = Color(0xFF3F007D)
private val SpinnerPurpleLight = Color(0xFFE8D4FF)
private val SpinnerDisabled = Color(0xFF4A4453)
private val SpinnerDisabledText = Color(0xFF7A7080)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeSpinner(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    label: String = "Select an option",
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            enabled = enabled,
            value = selectedOption,
            onValueChange = {},
            label = {
                Text(
                    text = label,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SpinnerPurple,
                unfocusedBorderColor = SpinnerPurpleLight.copy(alpha = 0.4f),
                disabledBorderColor = SpinnerDisabled,
                focusedLabelColor = SpinnerPurpleLight,
                unfocusedLabelColor = LightTextColor.copy(alpha = 0.7f),
                disabledLabelColor = SpinnerDisabledText,
                focusedTextColor = LightTextColor,
                unfocusedTextColor = LightTextColor,
                disabledTextColor = SpinnerDisabledText,
                focusedTrailingIconColor = SpinnerPurpleLight,
                unfocusedTrailingIconColor = LightTextColor.copy(alpha = 0.7f),
                disabledTrailingIconColor = SpinnerDisabled,
            )
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}