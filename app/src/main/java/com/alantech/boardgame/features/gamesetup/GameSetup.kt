package com.alantech.boardgame.features.gamesetup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.features.gamesetup.components.AddPlayerDialog
import com.alantech.boardgame.features.gamesetup.components.GameSetupTopBar
import com.alantech.boardgame.features.gamesetup.components.HouseRulesSection
import com.alantech.boardgame.features.gamesetup.components.PlayerSection
import com.alantech.boardgame.utils.DialogListener

@Composable
fun GameSetupScreen(
    onBackClick: () -> Unit = {},
    onStartGameClick: () -> Unit = {}
) {

    var showAddingMemberDialog by remember { mutableStateOf(false) }
    val dialogListener = remember { object : DialogListener {
        override fun onConfirm() {
            showAddingMemberDialog = false
        }
        override fun onCancel() {
            showAddingMemberDialog = false
        }
        override fun onDismiss() {
            showAddingMemberDialog = false
        }
    } }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF15101C))
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        GameSetupTopBar(onBackClick = onBackClick)
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            PlayerSection(){
                showAddingMemberDialog = !showAddingMemberDialog
            }
            Spacer(modifier = Modifier.height(48.dp))
            HouseRulesSection()
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStartGameClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC084FC)
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color(0xFF4C1D95)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Game",
                color = Color(0xFF4C1D95),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    AnimatedVisibility(
        visible = showAddingMemberDialog
    ) {
        AddPlayerDialog(dialogListener)
    }

}

@Preview
@Composable
private fun GameSuPV() {
    GameSetupScreen {  }
}