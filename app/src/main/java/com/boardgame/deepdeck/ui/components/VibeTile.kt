package com.boardgame.deepdeck.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme

/** Icon for a vibe `icon_key` (party|friends|drinking|love|family|work|couple|solo). */
@Composable
fun vibeIconPainter(iconKey: String): Painter = when (iconKey) {
    "party" -> painterResource(R.drawable.ic_party)
    "friends" -> painterResource(R.drawable.ic_friend)
    "drinking" -> painterResource(R.drawable.ic_beer)
    "love", "couple" -> painterResource(R.drawable.ic_love)
    "family" -> rememberVectorPainter(Icons.Rounded.FamilyRestroom)
    "work" -> rememberVectorPainter(Icons.Rounded.WorkOutline)
    "solo" -> rememberVectorPainter(Icons.Rounded.SelfImprovement)
    else -> painterResource(R.drawable.ic_party)
}

/**
 * Large gradient vibe tile ("Who are you with?"). Primary navigation on Home.
 */
@Composable
fun VibeTile(
    name: String,
    iconKey: String,
    colorStart: Color,
    colorEnd: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val painter = vibeIconPainter(iconKey)
    Box(
        modifier = modifier
            .aspectRatio(1.18f)
            .glow(colorEnd, radius = 20.dp, alpha = 0.22f)
            .clip(DeepTalkShapes.lg)
            .background(Brush.linearGradient(listOf(colorStart, colorEnd)))
            .border(1.dp, Color.White.copy(alpha = 0.18f), DeepTalkShapes.lg)
            .pressable(onClick = onClick, onClickLabel = name)
    ) {
        // Oversized decorative icon
        Image(
            painter = painter,
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 18.dp, y = 18.dp)
                .rotate(-14f)
                .alpha(0.18f)
        )
        // Soft top highlight
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.14f), Color.Transparent))
                )
        )
        Column(Modifier.padding(16.dp).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.22f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = name,
                style = DeepTalkTheme.type.title,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = DeepTalkTheme.type.label,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1
                )
            }
        }
    }
}
