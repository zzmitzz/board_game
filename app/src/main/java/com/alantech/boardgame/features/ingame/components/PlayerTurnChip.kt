package com.alantech.boardgame.features.ingame.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.theme.LightDialogBackground
import com.alantech.boardgame.ui.theme.LightSecondTextOBG

@Composable
fun PlayerTurnChip(playerName: String) {
    val plusJakarta = FontFamily(Font(R.font.plus_jakarta_sans))
    
    Row(
        modifier = Modifier
            .background(color = LightDialogBackground, shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = LightSecondTextOBG,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.player_turn_format, playerName),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = plusJakarta
        )
    }
}
