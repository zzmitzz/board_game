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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alantech.boardgame.R
import com.alantech.boardgame.features.mylibrary.ui.LibraryTextField
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.ui.theme.LightTextOnBackground
import com.alantech.boardgame.utils.PlusJakartaSans

@Composable
fun AddCardScreen(
    vm: AddCardVM,
    packId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
) {
    val form by vm.form.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> vm.onImageSelected(uri) }

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
                    contentDescription = stringResource(R.string.back),
                    tint = LightTextOnBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_card),
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

            LibraryTextField(
                value = form.category,
                onValueChange = vm::onCategoryChange,
                label = stringResource(R.string.card_category_label),
                placeholder = stringResource(R.string.card_category_placeholder)
            )

            LibraryTextField(
                value = form.description,
                onValueChange = vm::onDescriptionChange,
                label = stringResource(R.string.card_content_label),
                placeholder = stringResource(R.string.card_content_placeholder),
                singleLine = false
            )

            LibraryTextField(
                value = form.hint,
                onValueChange = vm::onHintChange,
                label = stringResource(R.string.card_hint_label),
                placeholder = stringResource(R.string.card_hint_placeholder),
                singleLine = false
            )

            CardImagePicker(
                uri = form.mediaImageUri?.toString(),
                onClick = { imagePicker.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { vm.save(packId, onSaved) },
                enabled = form.description.isNotBlank() && !form.isSaving,
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
                        text = stringResource(R.string.save_card),
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
private fun CardImagePicker(uri: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(LightPrimary, RoundedCornerShape(16.dp))
            .border(1.dp, LightSecondTextOBG.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = "Card Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = LightSecondTextOBG,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.tap_add_card_image),
                    color = LightTextColor.copy(alpha = 0.5f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp
                )
            }
        }
    }
}
