package com.alantech.boardgame.features.ingame.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.theme.LightSecondTextOBG




@Composable
fun InGameHeader(
    gameName: String,
    roundText: String,
    onCloseClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSettingsClick: () -> Unit
) = ActiveGameTopBar(
    gameName = gameName,
    roundText = roundText,
    onCloseClick = onCloseClick,
    onPauseClick = onPauseClick,
    onSettingsClick = onSettingsClick
)


@Composable
fun ActiveGameTopBar(
    gameName: String,
    roundText: String,
    onCloseClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onPauseClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Info",
                    tint = Color.White
                )
            }
        }
        
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gameName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = plusJakarta
            )
            Text(
                text = roundText.uppercase(),
                color = LightSecondTextOBG,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = plusJakarta
            )
        }
        
        IconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}
