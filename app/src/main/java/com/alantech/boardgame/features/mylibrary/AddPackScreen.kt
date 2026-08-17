package com.alantech.boardgame.features.mylibrary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.ui.theme.LightTextOnBackground
import com.alantech.boardgame.utils.PlusJakartaSans
import com.alantech.boardgame.features.mylibrary.ui.LibraryTextField

@Composable
fun AddPackScreen(
    vm: AddPackVM,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
) {
    val form by vm.form.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> vm.onCoverImageSelected(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LightTextOnBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Pack",
                color = LightTextOnBackground,
                fontFamily = PlusJakartaSans,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            CoverImagePicker(
                uri = form.coverImageUri?.toString(),
                onClick = { imagePicker.launch("image/*") }
            )

            LibraryTextField(
                value = form.title,
                onValueChange = vm::onTitleChange,
                label = "Title *",
                placeholder = "e.g. Party Questions"
            )

            LibraryTextField(
                value = form.description,
                onValueChange = vm::onDescriptionChange,
                label = "Description",
                placeholder = "Short description of this pack",
                singleLine = false
            )

            LibraryTextField(
                value = form.tag,
                onValueChange = vm::onTagChange,
                label = "Tag / Keywords",
                placeholder = "e.g. fun, party, friends"
            )

            HeatLevelSlider(
                value = form.heatLevel,
                onValueChange = vm::onHeatLevelChange
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LibraryTextField(
                    value = form.estimateTimePlay.toString(),
                    onValueChange = { vm.onEstimateTimeChange(it.toIntOrNull() ?: form.estimateTimePlay) },
                    label = "Est. Time (min)",
                    placeholder = "30",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    leadingIcon = { Icon(Icons.Default.Timer, null, tint = LightSecondTextOBG, modifier = Modifier.size(18.dp)) }
                )
                LibraryTextField(
                    value = form.suggestNumberPlayers.toString(),
                    onValueChange = { vm.onSuggestPlayersChange(it.toIntOrNull() ?: form.suggestNumberPlayers) },
                    label = "Players",
                    placeholder = "2",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    leadingIcon = { Icon(Icons.Default.People, null, tint = LightSecondTextOBG, modifier = Modifier.size(18.dp)) }
                )
            }

            LibraryTextField(
                value = form.howToPlay,
                onValueChange = vm::onHowToPlayChange,
                label = "How to Play",
                placeholder = "Explain the rules...",
                singleLine = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { vm.save(onSaved) },
                enabled = form.title.isNotBlank() && !form.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightSecondTextOBG)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "Save Pack",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CoverImagePicker(uri: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(LightPrimary, RoundedCornerShape(16.dp))
            .border(1.dp, LightSecondTextOBG.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = "Cover Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = LightSecondTextOBG,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to add cover image",
                    color = LightTextColor.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun HeatLevelSlider(value: Int, onValueChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Whatshot, null, tint = LightSecondTextOBG, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Heat Level: $value / 5",
                color = LightTextColor,
                fontFamily = PlusJakartaSans,
                fontSize = 13.sp
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = LightSecondTextOBG,
                activeTrackColor = LightSecondTextOBG,
                inactiveTrackColor = LightSecondTextOBG.copy(alpha = 0.2f)
            )
        )
    }
}
