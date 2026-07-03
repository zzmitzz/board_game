package com.alantech.boardgame.features.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.features.home.screen.HomeSearchBar
import com.alantech.boardgame.ui.theme.BoardGameTheme
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextOnBackground

@Composable
fun GameSearchStateful(
    onBackClick: () -> Unit,
    viewModel: GameSearchVM = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSearching = uiState is SealedGameSearchUIState.GameSearchUIState || uiState is SealedGameSearchUIState.Loading
    val query by viewModel.query.collectAsStateWithLifecycle()

    GameSearchingScreen(
        uiState = uiState,
        isSearching = isSearching,
        onLeadingIconClick = {
            if (isSearching) viewModel.clearQuery() else onBackClick()
        },
        onQueryChange = viewModel::onQueryChange,
        query = query
    )
}

@Composable
fun GameSearchingScreen(
    uiState: SealedGameSearchUIState,
    isSearching: Boolean,
    onLeadingIconClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    query: String= ""
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = onLeadingIconClick)
                        .padding(start = 12.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isSearching) Icons.Filled.Close else Icons.Filled.ArrowBackIosNew,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
                HomeSearchBar(
                    searchQuery = query,
                    onSearchTextChange = onQueryChange,
                    enableSearch = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (uiState) {
                is SealedGameSearchUIState.Loading -> LoadingSection()
                is SealedGameSearchUIState.InitUIState -> InitSection(state = uiState)
                is SealedGameSearchUIState.GameSearchUIState -> SearchResultsSection(results = uiState.results)
                is SealedGameSearchUIState.Error -> ErrorSection(message = uiState.message)
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LightSecondTextOBG)
    }
}

@Composable
private fun InitSection(state: SealedGameSearchUIState.InitUIState) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "RECENT",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (state.recentSearch.isNotEmpty()) {
            LazyColumn {
                items(state.recentSearch) { pack ->
                    Text(
                        text = pack,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        } else {
            Text(
                text = "No recent search",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (state.suggestPacks.isNotEmpty()) {
            Text(
                text = "SUGGESTED",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn {
                items(state.suggestPacks, key = { it.id.orEmpty() }) { pack ->
                    SearchResultItem(pack = pack)
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(results: List<PacksPreview>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "SEARCH RESULTS",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            items(results, key = { it.id.orEmpty() }) { pack ->
                SearchResultItem(pack = pack)
            }
        }
    }
}

@Composable
private fun ErrorSection(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
    }
}

@Composable
private fun SearchResultItem(pack: PacksPreview) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = pack.thumb,
            contentDescription = pack.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LightPrimary)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pack.title.orEmpty(),
                color = LightSecondTextOBG,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!pack.keywordsSummarise.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pack.keywordsSummarise ?: "",
                    color = LightTextOnBackground.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameSearchingScreenPreview() {
    BoardGameTheme {
        GameSearchingScreen(
//            uiState = SealedGameSearchUIState.GameSearchUIState(
//                query = "Card",
//                results = listOf(
//                    PacksPreview(
//                        id = "1",
//                        title = "Card Game",
//                        keywordsSummarise = "Fun • 2-4 players",
//                        thumb = null
//                    )
//                )
//            ),
            uiState = SealedGameSearchUIState.InitUIState(
                recentSearch = emptyList(),
                suggestPacks =
                    listOf(
                        PacksPreview(
                            id = "1",
                            title = "Card Game",
                            keywordsSummarise = "Fun • 2-4 players",
                            thumb = null
                        )
                    )
            ),
            isSearching = true,
            onLeadingIconClick = {},
            onQueryChange = {}
        )
    }
}
