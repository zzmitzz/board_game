package com.alantech.boardgame.features.pagedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.R
import com.alantech.boardgame.features.pagedetail.components.CardBadge
import com.alantech.boardgame.features.pagedetail.components.HeatLevelSection
import com.alantech.boardgame.features.pagedetail.components.HowToPlaySection
import com.alantech.boardgame.features.pagedetail.components.SampleCardItem
import com.alantech.boardgame.features.pagedetail.components.StatCircle
import com.alantech.boardgame.features.pagedetail.components.ThumbnailSection
import com.alantech.boardgame.ui.model.exampleText
import com.alantech.boardgame.ui.theme.LightBackground

@Composable
fun PageDetailScreen(
    onBackClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onUnlockClick: () -> Unit,
    viewModel: PageDetailVM = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    PageDetailTemplate(
        bottomBar = {
            PageDetailBottomBar(
                onUnlockClick = onUnlockClick
            )
        },
        modifier = Modifier,
        onBackClick = onBackClick
    ) {
        PageDetailContent(uiState = uiState)
    }
}

/**
 * Template level: Defines screen layout via slot composition
 */
@Composable
fun PageDetailTemplate(
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = bottomBar,
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                content()
                PageDetailTopBar(
                    onBackClick = onBackClick,
                )
            }
        }
    }
}

/**
 * Organism level: Composes molecules into the main scrollable content
 */
@Composable
fun PageDetailContent(
    uiState: PageDetailUIState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(color = LightBackground),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {

        item {
            HeroSection(
                badgeText = uiState.pack?.creator?.uppercase() ?: "PACK",
                title = uiState.pack?.titleCard ?: "",
                thumbnailUrl = uiState.pack?.thumbnail,
                onPreviewClick = {},
                modifier = Modifier
            )
        }
        item {
            StatsRow(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            HeatLevelSection(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item {
            SampleCardsSection()
        }
        item {
            HowToPlaySection(modifier = Modifier.padding(horizontal = 16.dp), instruction = exampleText)
        }
    }
}

// --- Organisms / Molecules ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageDetailTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                )
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                tint = Color.White,
                contentDescription = "Back",
            )
        }
    }
}

@Composable
private fun PageDetailBottomBar(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LightBackground,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$4.99",
                    style = MaterialTheme.typography.labelMedium.copy(
                        textDecoration = TextDecoration.LineThrough
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.free),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = onUnlockClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp)
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD0A5FF), // Brand Purple Light
                    contentColor = Color(0xFF3B0086) // Brand Dark Purple
                )
            ) {
                Text(
                    text = "Play this pack",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroSection(
    badgeText: String,
    title: String,
    thumbnailUrl: String?,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = remember { configuration.screenWidthDp.dp }
    Box {
        ThumbnailSection(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            screenWidth,
            imageUrl = thumbnailUrl
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(
                modifier.height(screenWidth / 2)
            )
            CardBadge(badgeText = badgeText)
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun StatsRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCircle(
            value = "50",
            label = "CARDS",
            drawable = R.drawable.card_size,
            modifier = Modifier.weight(1f)
        )
        StatCircle(
            value = "30m",
            label = "PLAY TIME",
            drawable = R.drawable.timer,
            modifier = Modifier.weight(1f)
        )
        StatCircle(
            value = "3+",
            label = "PLAYERS",
            drawable = R.drawable.player,
            modifier = Modifier.weight(1f)
        )
    }
}




@Composable
private fun SampleCardsSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sample_cards),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "SWIPE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFD0A5FF),
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SampleCardItem(
                    tag = "DARE",
                    content = "Let another player post a status on your social media."
                )
            }
            item {
                SampleCardItem(
                    tag = "TRUTH",
                    content = "What is the most embarrassing thing you've done in public?"
                )
            }
        }
    }
}





@Preview(
    heightDp = 2000
)
@Composable
private fun PreviewPageDetail() {
    PageDetailScreen(
        {}, {}, {}
    )
}