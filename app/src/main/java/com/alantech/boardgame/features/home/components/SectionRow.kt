package com.alantech.boardgame.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.alantech.boardgame.data.model.PacksPreview
import com.alantech.boardgame.data.model.SectionEntity
import com.alantech.boardgame.ui.theme.LightPrimary
import com.alantech.boardgame.ui.theme.LightSecondTextOBG
import com.alantech.boardgame.ui.theme.LightTextOnBackground

private enum class SectionUiType(val key: String) {
    Carousel("carousel"),
    Grid("grid"),
    List("list"),
    CardLarge("card_large");

    companion object {
        fun from(value: String?) = entries.firstOrNull { it.key == value } ?: Carousel
    }
}

@Composable
fun SectionRow(
    section: SectionEntity,
    packs: List<PacksPreview>,
    onCardClick: (String) -> Unit,
    onSeeAllClick: (String) -> Unit= {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = section.name.orEmpty(),
            modifier = Modifier.padding(horizontal = 24.dp),
            onSeeAllClick = { onSeeAllClick(section.id.orEmpty()) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (SectionUiType.from(section.uiType)) {
            SectionUiType.Carousel -> CarouselSection(packs, onCardClick)
            SectionUiType.Grid -> GridSection(packs, onCardClick)
            SectionUiType.List -> ListSection(packs, onCardClick)
            SectionUiType.CardLarge -> CardLargeSection(packs, onCardClick)
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier, onSeeAllClick: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = LightTextOnBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "See all",
            color = LightSecondTextOBG,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable {
                onSeeAllClick()
            }
        )
    }
}

@Composable
private fun CarouselSection(
    packs: List<PacksPreview>,
    onCardClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(packs, key = { it.id.orEmpty() }) { pack ->
            PackThumbCard(pack = pack, onClick = { onCardClick(pack.id.orEmpty()) })
        }
    }
}

@Composable
private fun GridSection(
    packs: List<PacksPreview>,
    onCardClick: (String) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(packs, key = { it.id.orEmpty() }) { pack ->
            PackThumbCard(
                pack = pack,
                modifier = Modifier.width(80.dp),
                onClick = { onCardClick(pack.id.orEmpty()) }
            )
        }
    }
}

@Composable
 fun ListSection(
    packs: List<PacksPreview>,
    onCardClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        packs.forEach { pack ->
            PackListItem(pack = pack, onClick = { onCardClick(pack.id.orEmpty()) })
        }
    }
}

@Composable
private fun CardLargeSection(
    packs: List<PacksPreview>,
    onCardClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(packs, key = { it.id.orEmpty() }) { pack ->
            PackLargeCard(pack = pack, onClick = { onCardClick(pack.id.orEmpty()) })
        }
    }
}

@Composable
private fun PackThumbCard(
    pack: PacksPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = pack.thumb,
                contentDescription = pack.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Text(
            text = pack.title.orEmpty(),
            style = MaterialTheme.typography.labelSmall,
            color = LightTextOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PackListItem(
    pack: PacksPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightPrimary)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = pack.thumb,
                contentDescription = pack.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pack.title.orEmpty(),
                color = LightTextOnBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            pack.keywordsSummarise?.let { keywords ->
                Text(
                    text = keywords,
                    color = LightSecondTextOBG,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PackLargeCard(
    pack: PacksPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LightPrimary)
        ) {
            AsyncImage(
                model = pack.thumb,
                contentDescription = pack.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = pack.title.orEmpty(),
            color = LightTextOnBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        pack.keywordsSummarise?.let { keywords ->
            Text(
                text = keywords,
                color = LightSecondTextOBG,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
