package com.kanhaji.attendancetracker.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun GenericLazyColumn(
    itemCount: Int = 100,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = ListState.value,
    modifier: Modifier = Modifier,
    // Card customization parameters
    onItemClick: ((Int) -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.outlinedCardColors(),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    interactionSource: MutableInteractionSource? = null,
    // Visual customization parameters
    externalCornerSize: Dp = 24.dp,
    internalCornerSize: Dp = 8.dp,
    itemSpacing: Dp = 4.dp,
    columnPadding: Dp = 16.dp,
    topPadding: Dp = 16.dp,
    cardContent: @Composable (Int) -> Unit
) {
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .padding(horizontal = columnPadding),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + topPadding,
            bottom = contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
            end = contentPadding.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
        ),
        state = listState,
    ) {
        items(itemCount) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(itemSpacing)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                val source = interactionSource ?: remember { MutableInteractionSource() }
                val isHovered = source.collectIsHoveredAsState().value

                // Track hovered index
                LaunchedEffect(isHovered) {
                    hoveredIndex = if (isHovered) index else null
                }

                val scale = animateScale(isHovered = isHovered, hoverScale = 1.039f)

                val animatedShape = animateCornerRadius(
                    isHovered = isHovered,
                    itemCount = itemCount,
                    index = index,
                    hoveredIndex = hoveredIndex,
                    externalCornerSize = externalCornerSize,
                    internalCornerSize = internalCornerSize
                )

                if (onItemClick != null) {
                    OutlinedCard(
                        onClick = { onItemClick(index) },
                        modifier = Modifier.fillMaxWidth().scale(scale)
//                            .pointerHoverIcon(PointerIcon.Hand)
                        ,
                        enabled = enabled,
                        shape = animatedShape,
                        colors = colors,
                        elevation = elevation,
                        border = border,
                        interactionSource = source
                    ) {
                        cardContent(index)
                    }
                } else {
                    OutlinedCard(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth().scale(scale)
//                            .pointerHoverIcon(PointerIcon.Hand)
                        ,
                        shape = animatedShape,
                        colors = colors,
                        elevation = elevation,
                        border = border,
                        interactionSource = source
                    ) {
                        cardContent(index)
                    }
                }
            }
        }
    }
}

object ListState{
    lateinit var value: LazyListState
}