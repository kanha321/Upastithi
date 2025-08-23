package com.kanhaji.basics.composables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

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