package com.kanhaji.basics.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.entity.SettingItems

object ListState {
    lateinit var value: LazyListState
}

data class Group<T>(
    val header: String,
    val items: List<T>
)

@Composable
fun <T> GenericLazyColumn(
    items: List<T>? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = ListState.value,
    modifier: Modifier = Modifier,
    // Generic list of items with key selector
    keySelector: ((T) -> Any)? = null,
    // ... other parameters remain the same
    onItemClick: ((Int) -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    interactionSource: MutableInteractionSource? = null,
    externalCornerSize: Dp = 24.dp,
    internalCornerSize: Dp = 8.dp,
    itemSpacing: Dp = 4.dp,
    columnPadding: Dp = 16.dp,
    topPadding: Dp = 16.dp,
    cardContent: @Composable (Int, T) -> Unit
) {
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = columnPadding),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + topPadding,
            bottom = contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
            end = contentPadding.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
        ),
        state = listState,
    ) {
        items(
            count = items?.size ?: 0,
            key = { index ->
                // Use the key selector if both items and keySelector are provided
                if (items != null && keySelector != null) {
                    items.getOrNull(index)?.let(keySelector) ?: index
                } else {
                    index
                }
            }
        ) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(itemSpacing)
                    .wrapContentHeight()
                    .animateItem(),
                contentAlignment = Alignment.Center
            ) {
                val source = interactionSource ?: remember { MutableInteractionSource() }
                val isHovered = source.collectIsHoveredAsState().value

                LaunchedEffect(isHovered) {
                    hoveredIndex = if (isHovered) index else null
                }

                val scale = animateScale(isHovered = isHovered, hoverScale = 1.039f)

                val animatedShape = animateCornerRadius(
                    isHovered = isHovered,
                    itemCount = items?.size ?: 0,
                    index = index,
                    hoveredIndex = hoveredIndex,
                    externalCornerSize = externalCornerSize,
                    internalCornerSize = internalCornerSize
                )

                if (onItemClick != null) {
                    OutlinedCard(
                        onClick = { onItemClick(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale),
                        enabled = enabled,
                        shape = animatedShape,
                        colors = colors,
                        elevation = elevation,
                        border = border,
                        interactionSource = source
                    ) {
                        cardContent(
                            index,
                            items?.get(index) ?: error("Item at index $index is null")
                        )
                    }
                } else {
                    OutlinedCard(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale),
                        shape = animatedShape,
                        colors = colors,
                        elevation = elevation,
                        border = border,
                        interactionSource = source
                    ) {
                        cardContent(
                            index,
                            items?.get(index) ?: error("Item at index $index is null")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T> GroupedLazyColumn(
    groups: List<Group<T>>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = ListState.value,
    modifier: Modifier = Modifier,
    keySelector: ((T) -> Any)? = null,
    onItemClick: ((groupIndex: Int, itemIndex: Int, item: T) -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    interactionSource: MutableInteractionSource? = null,
    externalCornerSize: Dp = 24.dp,
    internalCornerSize: Dp = 8.dp,
    itemSpacing: Dp = 4.dp,
    columnPadding: Dp = 16.dp,
    topPadding: Dp = 16.dp,
    headerContent: @Composable (String) -> Unit = {
        if (it.isNotBlank())
            Text(
                text = it,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
            )
        else Spacer(modifier = Modifier.padding(vertical = 4.dp))
    },
    cardContent: @Composable (groupIndex: Int, itemIndex: Int, item: T) -> Unit
) {
    var hoveredItem by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    // Pair<groupIndex, itemIndex>

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = columnPadding),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + topPadding,
            bottom = contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateStartPadding(LayoutDirection.Ltr),
            end = contentPadding.calculateEndPadding(LayoutDirection.Ltr)
        ),
        state = listState
    ) {
        groups.forEachIndexed { groupIdx, group ->

            // Group header
            item(key = "header-$groupIdx-${group.header}") {
                headerContent(group.header)
            }

            // Group items
            items(
                count = group.items.size,
                key = { idx ->
                    if (keySelector != null) {
                        keySelector(group.items[idx])
                    } else {
                        "$groupIdx-$idx-${group.header}"
                    }
                }
            ) { idx ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(itemSpacing)
                        .wrapContentHeight()
                        .animateItem(),
                    contentAlignment = Alignment.Center
                ) {
                    val source = interactionSource ?: remember { MutableInteractionSource() }
                    val isHovered = source.collectIsHoveredAsState().value

                    LaunchedEffect(isHovered) {
                        hoveredItem = if (isHovered) groupIdx to idx else null
                    }

                    val scale = animateScale(isHovered = isHovered, hoverScale = 1.039f)

                    val animatedShape = animateCornerRadius(
                        isHovered = isHovered,
                        itemCount = group.items.size,
                        index = idx,
                        hoveredIndex = if (hoveredItem?.first == groupIdx) hoveredItem?.second else null,
                        externalCornerSize = externalCornerSize,
                        internalCornerSize = internalCornerSize
                    )

                    OutlinedCard(
                        onClick = { onItemClick?.invoke(groupIdx, idx, group.items[idx]) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale),
                        enabled = enabled,
                        shape = animatedShape,
                        colors = colors,
                        elevation = elevation,
                        border = border,
                        interactionSource = source
                    ) {
                        cardContent(groupIdx, idx, group.items[idx])
                    }
                }
            }
        }
    }
}
