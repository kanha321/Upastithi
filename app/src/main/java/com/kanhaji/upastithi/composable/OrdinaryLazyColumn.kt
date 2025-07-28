package com.kanhaji.upastithi.composable

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun GenericLazyColumn(
    itemCount: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    listState: LazyListState = ListState.value,
    key: Any? = null,
    onItemClick: ((Int) -> Unit)? = null,
    enabled: Boolean = true,
    // THIS IS THE NEW PARAMETER FOR OUR COLOR RULE
    containerColor: ((index: Int) -> Color)? = null,
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
//    border: BorderStroke = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
    border: ((index: Int) -> BorderStroke)? = null,
    interactionSource: MutableInteractionSource? = null,
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
        items(count = itemCount, key = { index -> "$key-$index" }) { index ->
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

                // 1. This logic decides which color to use for the card at the current 'index'
                val currentContainerColor = if (containerColor != null) {
                    // If a rule was provided in the parameters, use it
                    containerColor(index)
                } else {
                    // Otherwise, use a default color from our theme
                    MaterialTheme.colorScheme.surface
                }

                val border = if (border != null) {
                    // If a border rule was provided, use it
                    border(index)
                } else {
                    // Otherwise, use a default border color
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                }

                // 2. Create the CardColors object using the color we just decided on.
                val cardProperties = CardDefaults.outlinedCardColors(
                    containerColor = if (currentContainerColor != MaterialTheme.colorScheme.surface) currentContainerColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )

                // 3. Now we use this new 'cardProperties' variable for the card's colors.
                if (onItemClick != null) {
                    OutlinedCard(
                        onClick = { onItemClick(index) },
                        modifier = Modifier.fillMaxWidth().scale(scale),
                        enabled = enabled,
                        shape = animatedShape,
                        colors = cardProperties, // Use the new properties here
                        elevation = elevation,
                        border = border,
                        interactionSource = source
                    ) {
                        cardContent(index)
                    }
                } else {
                    OutlinedCard(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth().scale(scale),
                        shape = animatedShape,
                        colors = cardProperties, // And also here
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

object ListState {
    lateinit var value: LazyListState
}