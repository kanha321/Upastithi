package com.kanhaji.upastithi.screen.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ResetTimetableDialog(
    onConfirmReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset to Original PDF Timetable?") },
        text = {
            Text("This will discard all your custom edits (added, modified, or deleted classes) and restore the timetable directly from the parsed PDF file.")
        },
        confirmButton = {
            Button(
                onClick = onConfirmReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset All Edits")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
