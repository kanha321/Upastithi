package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.entity.UpdatePriority
import com.kanhaji.basics.util.UpdateDownloadState
import com.kanhaji.basics.util.Updater
import kotlinx.coroutines.delay

@Composable
fun UpdateButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { Updater.showUpdateBottomSheet = true }
) {
    var showText by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600)
        showText = true
    }

    val isDownloading = Updater.downloadState is UpdateDownloadState.Downloading

    val accentColor = when (Updater.updatePriority) {
        UpdatePriority.OPTIONAL -> Color(0xFF2E7D32)     // Green
        UpdatePriority.RECOMMENDED -> Color(0xFFFF8F00) // Amber
        UpdatePriority.CRITICAL -> Color(0xFFD32F2F)    // Red
    }

    AnimatedContent(
        targetState = isDownloading,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "UpdateButtonState"
    ) { downloading ->
        if (downloading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                strokeWidth = 2.5.dp,
                color = accentColor
            )
        } else {
            Surface(
                onClick = onClick,
                modifier = modifier.padding(end = 4.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, accentColor),
                color = accentColor.copy(alpha = 0.08f),
                contentColor = accentColor,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Update,
                        contentDescription = "Update Available",
                        modifier = Modifier.size(18.dp)
                    )
                    AnimatedVisibility(showText) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Update",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
