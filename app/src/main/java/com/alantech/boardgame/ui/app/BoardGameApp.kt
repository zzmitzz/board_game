package com.alantech.boardgame.ui.app

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.alantech.boardgame.R
import com.alantech.boardgame.navigation.NavigationGraph
import com.alantech.boardgame.ui.state.BoardGameAppState
import com.alantech.boardgame.ui.theme.LightBackground
import com.alantech.boardgame.ui.theme.LightDialogBackground
import com.alantech.boardgame.ui.theme.LightOnPrimary
import com.alantech.boardgame.utils.BlurBackgroundDialog
import com.alantech.boardgame.utils.PlusJakartaSans


val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@Composable
fun BoardGameApp(
    appState: BoardGameAppState
) {
    val snackBarState = remember { SnackbarHostState() }
    val networkState = appState.isOffline.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalSnackbarHostState provides snackBarState
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBarState)
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = LightBackground
                    )
            ){
                NavigationGraph(
                    appState
                )
            }
        }
    }

    AnimatedVisibility(visible = networkState.value) {
        BlurBackgroundDialog(
            null
        ) {
            NetworkLoss()
        }
    }

}

@Preview
@Composable
fun NetworkLoss(){
    val lottieShuffle = rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.no_connection)
    )
    val context = LocalContext.current
    val openNetworkDialog = remember {
        {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Opens a bottom-sheet dialog inside your app (Android 10+)
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                // Opens the full Wi-Fi settings page (Android 9 and lower)
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            context.startActivity(intent)
        }

    }
    BlurBackgroundDialog(
        null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(color = LightDialogBackground, RoundedCornerShape(24.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Text(
                text = stringResource(R.string.network_connection_fail),
                color = LightOnPrimary,
                fontFamily = PlusJakartaSans,
                fontSize = 24.sp
            )
            LottieAnimation(
                composition = lottieShuffle.value,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = openNetworkDialog
            ) {
                Text(
                    text = stringResource(R.string.open_network_settings),
                )
            }


        }
    }
}