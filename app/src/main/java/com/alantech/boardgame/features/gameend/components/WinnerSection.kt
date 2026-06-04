package com.alantech.boardgame.features.gameend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.utils.PlusJakartaSans

@Composable
fun WinnerSection(gamePlayer: GamePlayer, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        ) {
            // Glow effect behind
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        drawCircle(
                            color = Color(0xFF6B21A8).copy(alpha = 0.5f),
                            radius = size.width / 2 + 30f
                        )
                    }
                    .border(3.dp, Color(0xFF9333EA), CircleShape)
                    .clip(CircleShape)
                    .background(gamePlayer.color.copy(
                        alpha = 0.9f
                    )),
                contentAlignment = Alignment.Center
            ){
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(90.dp)
                )
            }
            
            // WINNER badge
            Row(
                modifier = Modifier
                    .offset(y = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFD700))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.star_on),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WINNER",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = gamePlayer.name,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White
        )
        
        Text(
            text = "The Party Animal",
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = LightSecondTextOBG
        )
    }
}
