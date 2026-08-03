package com.alantech.boardgame.features.language

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.BoardGameApplication
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextOnBackground
import com.alantech.boardgame.utils.LanguageItem
import com.alantech.boardgame.utils.LocaleUtils
import com.alantech.boardgame.utils.findActivity
import com.alantech.boardgame.utils.listLanguageSupport

@Composable
fun LanguageSelectScreen(
    vm: LanguageSelectVM,
    onBackClick: () -> Unit = {},
) {
    var selectedCode by remember { mutableStateOf<LanguageItem?>(null) }
    val mContext = LocalContext.current
    LaunchedEffect(Unit) {
        selectedCode = vm.loadLanguage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(LightBackground)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        LanguageTopBar(onBackClick = onBackClick,
            onSaveClick = {
                selectedCode?.let { item ->
                    vm.saveLanguage(item) {
                        mContext.findActivity()?.let { activity ->
                            LocaleUtils.applyLocaleAndRecreate(activity, item.code)
                        } ?: run {
                            (mContext.applicationContext as BoardGameApplication).applyStoredLocale()
                        }
                    }
                }
            })
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightPrimary, RoundedCornerShape(16.dp)),
        ) {
            items(listLanguageSupport, key = { it.code }) { item ->
                LanguageRow(
                    item = item,
                    isSelected = item == selectedCode,
                    onClick = { selectedCode = item }
                )
                if (item != listLanguageSupport.last()) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(Color(0xFF262130), CircleShape)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = stringResource(R.string.select_language),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = LightSecondTextOBG,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable{
                    onSaveClick()
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)

        ){
            Text(
                text = stringResource(R.string.save),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LanguageRow(
    item: LanguageItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(item.flagRes),
            contentDescription = item.fullName,
            modifier = Modifier
                .width(40.dp)
        )
        Text(
            text = item.fullName,
            color = if (isSelected) LightSecondTextOBG else LightTextOnBackground,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = LightSecondTextOBG,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}