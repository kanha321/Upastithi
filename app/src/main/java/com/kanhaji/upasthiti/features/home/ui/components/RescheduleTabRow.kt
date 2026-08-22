package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember { listOf("↔️  Shift Class", "⇄  Swap Classes") }
    val tabContainerShape = RoundedCornerShape(24.dp)
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val tabOffsets = remember { mutableStateMapOf<Int, Float>() }
    val tabWidths = remember { mutableStateMapOf<Int, Float>() }
    val tabHeights = remember { mutableStateMapOf<Int, Float>() }

    val currentOffset = tabOffsets[selectedTabIndex] ?: 0f
    val currentWidth = tabWidths[selectedTabIndex] ?: 0f
    val currentHeight = tabHeights[selectedTabIndex] ?: 0f

    val animatedOffset by animateFloatAsState(
        targetValue = currentOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rescheduleTabIndicatorOffset"
    )

    val animatedWidth by animateFloatAsState(
        targetValue = currentWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rescheduleTabIndicatorWidth"
    )

    val animatedHeight by animateFloatAsState(
        targetValue = currentHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rescheduleTabIndicatorHeight"
    )

    val density = LocalDensity.current

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Surface(
            color = Color.Transparent,
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, tabContainerShape)
                    .clip(tabContainerShape)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                if (animatedWidth > 0f && animatedHeight > 0f) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = animatedOffset.roundToInt(), y = 0) }
                            .width(with(density) { animatedWidth.toDp() })
                            .height(with(density) { animatedHeight.toDp() })
                            .background(
                                color = selectedColor.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index

                        Surface(
                            onClick = { onTabSelected(index) },
                            modifier = Modifier
                                .weight(1f)
                                .onGloballyPositioned { coordinates ->
                                    tabOffsets[index] = coordinates.positionInParent().x
                                    tabWidths[index] = coordinates.size.width.toFloat()
                                    tabHeights[index] = coordinates.size.height.toFloat()
                                },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) selectedColor else unselectedColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
