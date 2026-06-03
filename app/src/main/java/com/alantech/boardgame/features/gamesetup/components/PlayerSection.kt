package com.alantech.boardgame.features.gamesetup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alantech.boardgame.R
import com.alantech.boardgame.ui.model.GamePlayer
import com.alantech.boardgame.utils.random

@Composable
fun PlayerSection(
    mPlayers: Set<GamePlayer> = emptySet(),
    onAddPlayerClick: () -> Unit = {},
    onDeleteAllPlayersClick: () -> Unit = {},
    onDeletePlayerClick: (Int) -> Unit = {},
    onNameChange: (id: Int, name: String) -> Unit = { _, _ -> }
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.whos_playing),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = mPlayers.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Player",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                onDeleteAllPlayersClick.invoke()
                            }
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "${mPlayers.size} Player(s)",
                    color = Color(0xFFD8A5FF),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color(0xFF2E1B46), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddPlayerItem() {
                onAddPlayerClick.invoke()
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                itemsIndexed(mPlayers.toList()){ index, player ->
                    PlayerItem(
                        name = player.name,
                        bgColor = player.color,
                        onNameChange = { onNameChange(player.id, it) }
                    ) {
                        onDeletePlayerClick.invoke(player.id)
                    }
                }
            }
        }

    }
}

@Composable
fun AddPlayerItem(
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            onClick.invoke()
        }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            val stroke = Stroke(
                width = 4f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
            val color = Color(0xFF4A4453)
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    color = color,
                    style = stroke,
                    cornerRadius = CornerRadius(size.width / 2)
                )
            }
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Change Players",
                tint = Color(0xFFA19AA8),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "ADD",
            color = Color(0xFFA19AA8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PlayerItem(
    name: String,
    bgColor: Color,
    onNameChange: (String) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    var currentName by remember(name) { mutableStateOf(name) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(76.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, Color(0xFFB975FF), CircleShape)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            bgColor.copy(
                                alpha = 0.9f
                            ), CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(24.dp)
                    .background(Color(0xFFB975FF), CircleShape)
                    .border(2.dp, Color(0xFF15101C), CircleShape)
                    .clickable {
                        onClick.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = Color(0xFF3F007D),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = currentName,
            onValueChange = { value ->
                currentName = value
                onNameChange(value)
            },
            modifier = Modifier
                .width(72.dp)
                .focusRequester(focusRequester)
                .clickable { focusRequester.requestFocus() },
            singleLine = true,
            cursorBrush = SolidColor(Color(0xFFB975FF)),
            textStyle = TextStyle(
                color = Color(0xFFA19AA8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun OtherPlayerItem(name: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
