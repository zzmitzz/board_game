package com.alantech.boardgame.features.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.ManageHistory
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.R
import com.alantech.boardgame.features.setting.composable.GameSetupTopBar
import com.alantech.boardgame.ui.theme.BoardGameTheme
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextColor
import com.alantech.boardgame.ui.theme.LightTextOnBackground
import com.alantech.boardgame.utils.openWebPage

private data class SettingItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)


@Composable
fun SettingScreen(
    onBackClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onPlayHistoryClick: () -> Unit = {},
    onMyLibraryClick: () -> Unit = {},
) {
    val mContext = LocalContext.current
    val items = listOf(
        SettingItem(
            Icons.Default.Language,
            stringResource(R.string.language)
        ) { onLanguageClick() },
        SettingItem(Icons.Default.Star, stringResource(R.string.rate_us)) {
        },
        SettingItem(Icons.Default.DocumentScanner, stringResource(R.string.term_condition)) {

            mContext.openWebPage("https://inclined-scarlet-jn0reqb4.edgeone.dev/")
        },
        SettingItem(Icons.Default.PrivacyTip, stringResource(R.string.privacy_policy)) {

            mContext.openWebPage("https://uncertain-amethyst-pvpmhpc5.edgeone.dev/")
        },
        SettingItem(
            Icons.Default.History,
            stringResource(R.string.play_history)
        ) { onPlayHistoryClick() },
        SettingItem(
            Icons.Default.LibraryBooks,
            "My Library"
        ) { onMyLibraryClick() },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(color = LightBackground)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        GameSetupTopBar(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightPrimary, RoundedCornerShape(16.dp))
        ) {
            items.forEachIndexed { index, item ->
                SettingRow(item = item)
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.developer),
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun SettingRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = LightSecondTextOBG,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = item.label,
            color = LightTextOnBackground,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = LightTextColor.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    BoardGameTheme {
        SettingScreen()
    }
}