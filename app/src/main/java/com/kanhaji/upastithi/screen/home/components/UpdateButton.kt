package com.kanhaji.upastithi.screen.home.components

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanhaji.basics.util.Updater
import com.kanhaji.basics.util.Updater.isDownloading
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateButton(
    modifier: Modifier = Modifier
) {
    var showText by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Small delayed entrance animation for the text
    LaunchedEffect(Unit) {
        delay(600)
        showText = true
    }

    AnimatedContent(
        targetState = Updater.isDownloading,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "UpdateButtonState"
    ) { isDownloading ->
        when (isDownloading) {
            null, false -> {
                // Idle state → show button
                Surface(
                    onClick = {
                        scope.launch {
                            Updater.startDownload(context)
                        }
                    },
                    modifier = modifier,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    color = Color.Transparent,
                    contentColor = Color(0xFF2E7D32),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .animateContentSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Update,
                            contentDescription = "Check for Updates"
                        )
                        AnimatedVisibility(showText) {
                            Row {
                                Spacer(Modifier.width(8.dp))
                                Text(text = "Update", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            true -> {
                // Downloading state → show loading indicator
                LoadingIndicator(
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}
