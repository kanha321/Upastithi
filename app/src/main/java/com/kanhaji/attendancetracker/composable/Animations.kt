package com.kanhaji.attendancetracker.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun animateCornerRadius(
    isHovered: Boolean,
    itemCount: Int,
    index: Int,
    hoveredIndex: Int?,
    externalCornerSize: Dp,
    internalCornerSize: Dp
): RoundedCornerShape {
    val isAboveHovered = hoveredIndex != null && index == hoveredIndex - 1
    val isBelowHovered = hoveredIndex != null && index == hoveredIndex + 1

    val topStartRadius = animateDpAsState(
        targetValue = when {
            isHovered -> externalCornerSize
            isBelowHovered -> externalCornerSize
            itemCount == 1 -> externalCornerSize
            index == 0 -> externalCornerSize
            else -> internalCornerSize
        },
        animationSpec = tween(300)
    ).value

    val topEndRadius = animateDpAsState(
        targetValue = when {
            isHovered -> externalCornerSize
            isBelowHovered -> externalCornerSize
            itemCount == 1 -> externalCornerSize
            index == 0 -> externalCornerSize
            else -> internalCornerSize
        },
        animationSpec = tween(300)
    ).value

    val bottomStartRadius = animateDpAsState(
        targetValue = when {
            isHovered -> externalCornerSize
            isAboveHovered -> externalCornerSize
            itemCount == 1 -> externalCornerSize
            index == itemCount - 1 -> externalCornerSize
            else -> internalCornerSize
        },
        animationSpec = tween(300)
    ).value

    val bottomEndRadius = animateDpAsState(
        targetValue = when {
            isHovered -> externalCornerSize
            isAboveHovered -> externalCornerSize
            itemCount == 1 -> externalCornerSize
            index == itemCount - 1 -> externalCornerSize
            else -> internalCornerSize
        },
        animationSpec = tween(300)
    ).value

    return RoundedCornerShape(
        topStart = topStartRadius,
        topEnd = topEndRadius,
        bottomStart = bottomStartRadius,
        bottomEnd = bottomEndRadius
    )
}
@Composable
fun animateScale(
    isHovered: Boolean,
    hoverScale: Float = 1.015f,
    normalScale: Float = 1f,
    durationMs: Int = 300
): Float {
    return animateFloatAsState(
        targetValue = if (isHovered) hoverScale else normalScale,
        animationSpec = tween(durationMs)
    ).value
}

@Composable
fun animateBorderWidth(
    isSelected: Boolean,
    selectedWidth: Dp = 2.dp,
    unselectedWidth: Dp = 0.dp,
    durationMs: Int = 300
): Dp {
    return animateDpAsState(
        targetValue = if (isSelected) selectedWidth else unselectedWidth,
        animationSpec = tween(durationMs),
        label = "borderWidthAnimation"
    ).value
}

@Composable
fun animateSelectionColor(
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    durationMs: Int = 300
): Color {
    return animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(durationMs),
        label = "selectionColorAnimation"
    ).value
}

@Composable
fun animateTextScale(
    isSelected: Boolean,
    selectedScale: Float = 1.05f,
    unselectedScale: Float = 1f,
    durationMs: Int = 300
): Float {
    return animateFloatAsState(
        targetValue = if (isSelected) selectedScale else unselectedScale,
        animationSpec = tween(durationMs),
        label = "textScaleAnimation"
    ).value
}