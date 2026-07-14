package com.alantech.boardgame.features.home.section_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import com.alantech.boardgame.features.home.components.ListSection
import com.alantech.boardgame.utils.PlusJakartaSans


@Composable
fun SectionDetailScreens(
    onBackClick: () -> Unit = {},
    onCardClick: (String) -> Unit
) {
    val vmComposeScoped = rememberViewModelStoreOwner()
    val vm = hiltViewModel<SectionScreenScopedVM>(
        viewModelStoreOwner = vmComposeScoped
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    SectionDetailStateless(
        onBackClick = onBackClick,
        uiState = uiState,
        onCardClick = onCardClick
    )
}


@Composable
fun SectionDetailStateless(
    onBackClick: () -> Unit,
    uiState: SectionUIState,
    onCardClick: (String) -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
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
            Spacer(
                modifier = Modifier
                    .width(24.dp)
            )
            if(uiState is SectionUIState.Success){
                Text(
                    text = uiState.sectionEntity.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }

        when (uiState) {
            is SectionUIState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is SectionUIState.Success -> {
                ListSection(
                    packs = uiState.packs,
                    onCardClick = {
                        onCardClick(it)
                    }
                )
            }
            is SectionUIState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}