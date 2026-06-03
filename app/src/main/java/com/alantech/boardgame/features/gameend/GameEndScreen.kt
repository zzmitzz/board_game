package com.alantech.boardgame.features.gameend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.features.gameend.components.*
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.features.ingame.model.GamePlayerScore
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.utils.PlusJakartaSans
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun GameEndScreen(
    onBackClick: () -> Unit = {},
    onPlayAgainClick: () -> Unit = {},
    onRematchClick: () -> Unit = {},
    vm: InGameVM
) {
    GameEndContent(
        onBackClick = onBackClick,
        onPlayAgainClick = onPlayAgainClick,
        onRematchClick = onRematchClick,
        entries = remember(vm.gamePlayerManager.getGamePlayersScore()) {
            vm.gamePlayerManager.getGamePlayersScore().entries
                .map { it.key to it.value }
                .sortedWith { a, b -> a.second.compareTo(b.second) }
        }
    )
}

@Composable
private fun GameEndContent(
    onBackClick: () -> Unit = {},
    onPlayAgainClick: () -> Unit = {},
    onRematchClick: () -> Unit = {},
    entries: List<Pair<GamePlayer, GamePlayerScore>>,
) {
    val winnerName = entries.firstOrNull()?.first?.name.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color(0xFF262130), CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Results",
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                WinnerSection(winnerName = winnerName)
            }
            item {
                CapturedMomentsSection()
            }
            item {
                LeaderboardSection(entries = entries)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRematchClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF251F2A)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rematch",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = onPlayAgainClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Play Again",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private class GameEndPreviewProvider : PreviewParameterProvider<List<Pair<GamePlayer, GamePlayerScore>>> {
    override val values = sequenceOf(
        listOf(
            GamePlayer(id = 1, color = ComposeColor(0xFF9D4EDD), name = "Sarah") to
                    GamePlayerScore(numberCardCompleted = 8, numberCardForfeited = 1, timeSpent = 120),
            GamePlayer(id = 2, color = ComposeColor(0xFF4ADE80), name = "Mike") to
                    GamePlayerScore(numberCardCompleted = 6, numberCardForfeited = 2, timeSpent = 140),
            GamePlayer(id = 3, color = ComposeColor(0xFFF87171), name = "Jessica") to
                    GamePlayerScore(numberCardCompleted = 5, numberCardForfeited = 3, timeSpent = 100),
            GamePlayer(id = 4, color = ComposeColor(0xFFFBBF24), name = "David") to
                    GamePlayerScore(numberCardCompleted = 3, numberCardForfeited = 4, timeSpent = 160),
        )
    )
}

@Preview(heightDp = 2000)
@Composable
private fun GameEndScreenPV(
    @PreviewParameter(GameEndPreviewProvider::class)
    entries: List<Pair<GamePlayer, GamePlayerScore>>
) {
    GameEndContent(entries = entries)
}