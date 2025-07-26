package com.kanhaji.attendancetracker.screen.home.components

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.kanhaji.attendancetracker.composable.GenericLazyColumn
import com.kanhaji.attendancetracker.data.TimeTable

@Composable
fun ClassesDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selected Date") },
        text = {
            GenericLazyColumn(
                itemCount = TimeTable.THURSDAY.size,
                listState = rememberLazyListState(),
                itemSpacing = 2.dp
            ) { index ->
                ClassCardSingle(TimeTable.THURSDAY[index])
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}