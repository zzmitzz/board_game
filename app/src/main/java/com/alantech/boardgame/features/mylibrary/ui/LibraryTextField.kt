package com.alantech.boardgame.features.mylibrary.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.ui.theme.LightTextOnBackground
import com.alantech.boardgame.utils.PlusJakartaSans

@Composable
fun LibraryTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = PlusJakartaSans) },
        placeholder = { Text(placeholder, fontFamily = PlusJakartaSans, color = LightTextColor.copy(alpha = 0.4f)) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        maxLines = if (singleLine) 1 else 6,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LightTextOnBackground,
            unfocusedTextColor = LightTextOnBackground,
            focusedBorderColor = LightSecondTextOBG,
            unfocusedBorderColor = LightSecondTextOBG.copy(alpha = 0.3f),
            focusedLabelColor = LightSecondTextOBG,
            unfocusedLabelColor = LightTextColor.copy(alpha = 0.6f),
            cursorColor = LightSecondTextOBG,
            focusedContainerColor = LightPrimary,
            unfocusedContainerColor = LightPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
