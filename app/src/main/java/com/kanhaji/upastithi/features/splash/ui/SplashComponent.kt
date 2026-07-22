package com.kanhaji.upastithi.features.splash.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.upastithi.features.home.ui.HomeScreen
import com.kanhaji.upastithi.util.UpasthitiUtils
import kotlinx.coroutines.delay

@Composable
fun SplashComponent() {
    val navigator = LocalNavigator.currentOrThrow
    var startAnimation by remember { mutableStateOf(false) }

    val logoOffsetAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -100f,
        animationSpec = tween(
            durationMillis = 800,
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        ),
        label = "logoOffset"
    )

    val logoScaleAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = 800,
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        ),
        label = "logoScale"
    )

    val logoAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 100,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "logoAlpha"
    )

    val titleOffsetAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = 200,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "titleOffset"
    )

    val titleAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 300,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "titleAlpha"
    )

    val subtitleAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 500,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "subtitleAlpha"
    )

    val versionAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = 700,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "versionAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        navigator.replace(HomeScreen)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(88.dp)
                    .scale(logoScaleAnimation)
                    .alpha(logoAlphaAnimation)
                    .graphicsLayer {
                        translationY = logoOffsetAnimation
                    },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChecklistRtl,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Upasthiti",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .alpha(titleAlphaAnimation)
                    .graphicsLayer {
                        translationY = titleOffsetAnimation
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "For MNNIT MCA Students",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(subtitleAlphaAnimation)
            )
        }

        Text(
            text = "Version " + UpasthitiUtils.appVersionName?.replace("v", ""),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(versionAlphaAnimation)
        )
    }
}
