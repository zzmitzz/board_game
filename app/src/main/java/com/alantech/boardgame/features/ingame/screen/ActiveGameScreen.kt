package com.alantech.boardgame.features.ingame.screen

import androidx.activity.ExperimentalActivityApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.features.ingame.components.ActionButtons
import com.alantech.boardgame.features.ingame.components.ActiveGameTopBar
import com.alantech.boardgame.features.ingame.components.ChallengeCard
import com.alantech.boardgame.features.ingame.components.PlayerTurnChip
import com.alantech.boardgame.ui.theme.LightBackground

@Composable
fun ActiveGameScreen(
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActiveGameTopBar(
            gameName = "Do or Drink",
            roundText = "ROUND 3/10",
            onCloseClick = onBackClick,
            onPauseClick = { },
            onSettingsClick = { }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PlayerTurnChip(playerName = "Player1")
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ChallengeCard(
            category = "DARE",
            challengeText = "Call your ex and tell them you miss their dog.",
            penaltyText = "Take 1 Shot",
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            ActionButtons(
                onComplete = { },
                onForfeit = { }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview
@Composable
private fun PVActiveGame() {
    ActiveGameScreen {  }
}