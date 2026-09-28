package com.boardgame.deepdeck.features.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.HomeSectionDto
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.ui.components.PackTile
import com.boardgame.deepdeck.ui.components.PackTileStyle
import com.boardgame.deepdeck.ui.components.SectionHeader
import com.boardgame.deepdeck.ui.components.SkeletonBox
import com.boardgame.deepdeck.ui.components.SkeletonLine
import com.boardgame.deepdeck.ui.components.packCoverKey
import com.boardgame.deepdeck.ui.components.staggeredEntrance
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.Spacing

private const val LIST_PREVIEW_COUNT = 4

/**
 * Server-driven section (`carousel | grid | list | card_large`) restyled with PackTile.
 * [onPackClick] receives (pack, sharedCoverKey).
 */
@Composable
fun SectionBlock(
    section: HomeSectionDto,
    onPackClick: (PacksPreview, String?) -> Unit,
    onSeeAllClick: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sectionId = section.id.orEmpty()
    val style = PackTileStyle.fromUiType(section.uiType)
    val source = "section-$sectionId"
    val packs = section.packs.filter { !it.id.isNullOrBlank() }
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = section.name.orEmpty(),
            subtitle = section.description?.takeIf { it.isNotBlank() },
            actionLabel = stringResource(R.string.see_all),
            onAction = { onSeeAllClick(sectionId, section.name) },
            modifier = Modifier.padding(horizontal = Spacing.gutter)
        )
        Spacer(Modifier.height(Spacing.md))
        val click: (PacksPreview) -> Unit = { pack ->
            onPackClick(pack, packCoverKey(source, pack.id.orEmpty()))
        }
        when (style) {
            PackTileStyle.Carousel, PackTileStyle.Large -> LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(if (style == PackTileStyle.Large) 16.dp else 14.dp)
            ) {
                itemsIndexed(packs, key = { _, p -> p.id.orEmpty() }) { index, pack ->
                    PackTile(
                        pack = pack,
                        style = style,
                        sharedKey = packCoverKey(source, pack.id.orEmpty()),
                        onClick = { click(pack) },
                        modifier = Modifier.staggeredEntrance(index)
                    )
                }
            }

            PackTileStyle.Compact -> LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp * 2 + 10.dp),
                contentPadding = PaddingValues(horizontal = Spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(packs, key = { _, p -> p.id.orEmpty() }) { index, pack ->
                    PackTile(
                        pack = pack,
                        style = style,
                        sharedKey = packCoverKey(source, pack.id.orEmpty()),
                        onClick = { click(pack) },
                        modifier = Modifier.staggeredEntrance(index)
                    )
                }
            }

            PackTileStyle.Row -> Column(
                modifier = Modifier.padding(horizontal = Spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                packs.take(LIST_PREVIEW_COUNT).forEachIndexed { index, pack ->
                    PackTile(
                        pack = pack,
                        style = style,
                        sharedKey = packCoverKey(source, pack.id.orEmpty()),
                        onClick = { click(pack) },
                        modifier = Modifier.staggeredEntrance(index)
                    )
                }
            }
        }
    }
}

/** Loading placeholder for a carousel section. */
@Composable
fun SectionSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        SkeletonLine(width = 160.dp, height = 18.dp, modifier = Modifier.padding(horizontal = Spacing.gutter))
        Spacer(Modifier.height(Spacing.md))
        Row(
            modifier = Modifier.padding(horizontal = Spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            repeat(3) {
                Column {
                    SkeletonBox(Modifier.width(148.dp).height(188.dp), DeepTalkShapes.lg)
                    Spacer(Modifier.height(10.dp))
                    SkeletonLine(width = 110.dp)
                }
            }
        }
    }
}
