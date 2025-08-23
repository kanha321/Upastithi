package com.kanhaji.basics.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CustomSnackbar(data: SnackbarData) {
    val timerProgress = remember { Animatable(1f) }

    // Animate the bar from full width to zero in 5 seconds, then dismiss
    LaunchedEffect(data) {
        timerProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(3000)
        )
        data.dismiss()
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.95f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { data.dismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(timerProgress.value)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun CustomSnackbarYesNo(
    data: SnackbarData,
    onYes: () -> Unit = {},
    onNo: () -> Unit = {}
) {
    val timerProgress = remember { Animatable(1f) }
    LaunchedEffect(data) {
        timerProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(3000)
        )
        data.dismiss()
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(0.95f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.visuals.message,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    onYes()
                    data.dismiss()
                }) {
                    Text("Yes")
                }
                IconButton(onClick = {
                    onNo()
                    data.dismiss()
                }) {
                    Text("No")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(timerProgress.value)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}


object MySnackBarObject {
    lateinit var snackbarHostState: SnackbarHostState
    var customContent: (@Composable (SnackbarData) -> Unit)? = null
}

@Composable
fun MySnackbarHost() {
    SnackbarHost(
        hostState = MySnackBarObject.snackbarHostState,
        snackbar = { data ->
            MySnackBarObject.customContent?.invoke(data) ?: CustomSnackbar(data)
        }
    )
}

suspend fun showSnackbar(
    message: String,
    customComposable: (@Composable (SnackbarData) -> Unit)? = null
) {
    MySnackBarObject.customContent = customComposable
    MySnackBarObject.snackbarHostState.currentSnackbarData?.dismiss()
    MySnackBarObject.snackbarHostState.showSnackbar(message)
}