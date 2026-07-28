package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun UpdateButton() {
    var showDownloadDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDownloadDialog = true }) {
        Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = "Update Available",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (showDownloadDialog) {
        DownloadDialog(onDismiss = { showDownloadDialog = false })
    }
}
