package com.boardgame.deepdeck.features.ingame.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.theme.LightDialogBackground
import com.boardgame.deepdeck.ui.theme.LightTextColor
import com.boardgame.deepdeck.utils.PlusJakartaSans

@Composable
fun CardHintDialog(cardHint: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(color = LightDialogBackground, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.card_hint),
            style = MaterialTheme.typography.titleLarge,
            color = LightTextColor,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = cardHint,
            style = MaterialTheme.typography.bodyMedium,
            color = LightTextColor,
            fontFamily = PlusJakartaSans
        )
    }
}