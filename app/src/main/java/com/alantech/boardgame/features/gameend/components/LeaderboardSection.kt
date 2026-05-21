package com.alantech.boardgame.features.gameend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.utils.PlusJakartaSans

@Composable
fun LeaderboardSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "Leaderboard",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
        
        LeaderboardItem(rank = 1, name = "Sarah", xp = "1500 XP", isWinner = true)
        Spacer(modifier = Modifier.height(12.dp))
        LeaderboardItem(rank = 2, name = "Mike", xp = "1200 XP", isWinner = false)
        Spacer(modifier = Modifier.height(12.dp))
        LeaderboardItem(rank = 3, name = "Jessica", xp = "950 XP", isWinner = false)
        Spacer(modifier = Modifier.height(12.dp))
        LeaderboardItem(rank = 4, name = "David", xp = "800 XP", isWinner = false)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "\"What happens in the game, stays in the game... mostly.\"",
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun LeaderboardItem(rank: Int, name: String, xp: String, isWinner: Boolean) {
    val backgroundColor = if (isWinner) Color(0xFF2A153E) else Color(0xFF1E1A22)
    val rankColor = if (isWinner) Color(0xFF9333EA) else Color.Gray
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(
                width = if (isWinner) 1.dp else 0.dp,
                color = if (isWinner) Color(0xFF5611A3) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rank.toString(),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = rankColor,
            modifier = Modifier.width(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            if (isWinner) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9333EA))
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.star_on),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = xp,
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        
        if (isWinner) {
            Text(
                text = "Winner",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
