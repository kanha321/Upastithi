package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun CriticalUpdateDialog(
    latestVersionName: String,
    onConfirmUpdate: () -> Unit,
    onExitApp: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Non-dismissible by tapping outside */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = "Critical Update",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        title = {
            Text(
                text = "Critical Update Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Version $latestVersionName contains critical system changes that are required for Upasthiti to function properly. Please update to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmUpdate,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onExitApp,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Exit App", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
