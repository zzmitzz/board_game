package com.alantech.boardgame.features.onboard.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alantech.boardgame.R


@Composable
fun OnBoardScreen(
    modifier: Modifier,
    onBack: () -> Unit,
    onNext: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content.invoke()
        Row(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text = stringResource(R.string.nav_back),
                    modifier = Modifier.clickable {
                        onBack.invoke()
                    }
                )
            }

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text = stringResource(R.string.nav_back),
                    modifier = Modifier.clickable {
                        onBack.invoke()
                    }
                )
            }
        }
    }
}


@Composable
fun ScreenA() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Screen A"
        )
    }
}

@Composable
fun ScreenB() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Screen B"
        )
    }
}

@Composable
fun ScreenC() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Screen C"
        )
    }
}



