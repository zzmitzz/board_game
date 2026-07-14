package com.alantech.boardgame.features.gamesetup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.theme.LightDialogBackground
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.utils.BlurBackgroundDialog
import com.alantech.boardgame.utils.DialogListener
import com.alantech.boardgame.utils.DialogPlayerListener
import com.alantech.boardgame.utils.PlusJakartaSans
import com.alantech.boardgame.utils.clickInterval


@Composable
fun AddPlayerDialog(
    dialogListener: DialogPlayerListener
) {

    var lastClickTime = remember { 0L }

    BlurBackgroundDialog(dialogListener) {
        var selectedCount by remember { mutableStateOf("1") }
        var customValue by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .background(color = LightDialogBackground, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.how_many_players),
                style = MaterialTheme.typography.titleLarge,
                color = LightTextColor,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecificPlayerBox(
                        modifier = Modifier.weight(1f),
                        1,
                        selectedCount == "1"
                    ) { selectedCount = "1" }
                    SpecificPlayerBox(
                        modifier = Modifier.weight(1f),
                        2,
                        selectedCount == "2"
                    ) { selectedCount = "2" }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpecificPlayerBox(
                        modifier = Modifier.weight(1f),
                        3,
                        selectedCount == "3"
                    ) { selectedCount = "3" }
                    CustomPlayerBox(
                        modifier = Modifier.weight(1f),
                        selectedCount == customValue,
                        customValue
                    ) {
                        customValue = it
                        selectedCount = it
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        dialogListener.onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.cancel_text),
                        color = Color(0xFFCFC2D6), // Light grayish purple from your code
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )
                }

                // 2. Confirm Selection Button
                Surface(
                    onClick = {
                        if(System.currentTimeMillis() - lastClickTime > clickInterval){
                            lastClickTime = System.currentTimeMillis()
                            if(selectedCount.isNotBlank()){
                                val players = selectedCount.toIntOrNull()
                                players?.let {
                                    dialogListener.onConfirm(it)
                                }
                            }
                        }
                    },
                    shape = CircleShape, // Makes it pill-shaped
                    color = Color(0xFFD7B4F3), // The bright lilac/purple from the image
                    tonalElevation = 8.dp, // Adds that slight glow/shadow effect
                    modifier = Modifier.padding(start = 16.dp).weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.confirm_selection),
                        color = Color(0xFF4B0082), // Dark purple text inside the pill
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpecificPlayerBox(
    modifier: Modifier,
    players: Int = 1,
    isSelected: Boolean = false,
    onClick: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .height(64.dp)
            .background(
                color = if (isSelected) Color(0xFFDDB7FF) else Color(0xFF39323D),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable {
                onClick(players)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = players.toString(),
            color = if (isSelected) Color(0xFF490080) else Color(0xFFDDB7FF),
            fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            fontFamily = PlusJakartaSans,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.players),
            color = if (isSelected) Color(0xFF490080) else Color(0xFFCFC2D6),
            fontWeight = FontWeight.Bold,
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

    }
}


@Composable
fun CustomPlayerBox(
    modifier: Modifier,
    isSelected: Boolean = false,
    customValue: String = "",
    onCustomValueChange: (String) -> Unit,
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .background(
                color = if (isSelected) Color(0xFFDDB7FF) else Color(0xFF39323D),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = customValue,
            onValueChange = {
                onCustomValueChange(it)
            },
            textStyle = TextStyle(
                color = if (isSelected) Color(0xFF490080) else Color(0xFFDDB7FF),
                fontWeight = if(isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                if (customValue.isEmpty()) {
                    Text(
                        text = "Custom",
                        color = Color(0xFFA19AA8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun SpecPlayerBox() {
    AddPlayerDialog(object : DialogPlayerListener() {
        override fun onCancel() {}
        override fun onDismiss() {}
        override fun onConfirm(numberPlayer: Int) {

        }
    })
}

