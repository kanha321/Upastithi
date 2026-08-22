package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.kanhaji.basics.util.Updater

@Composable
fun UpdateButton(
    onClick: () -> Unit = { Updater.showUpdateBottomSheet = true }
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = "Update Available",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
