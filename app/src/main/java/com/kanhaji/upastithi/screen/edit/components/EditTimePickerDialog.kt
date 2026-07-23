package com.kanhaji.upastithi.screen.edit.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class TimeStep {
    START_TIME,
    END_TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimePickerDialog(
    initialTimeRange: String,
    onConfirmTimeRange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parsedTimes = remember(initialTimeRange) {
        val parts = initialTimeRange.split("-").map { it.trim() }
        val startStr = parts.getOrNull(0) ?: "08:00"
        val endStr = parts.getOrNull(1) ?: "09:00"

        val startParts = startStr.split(":").mapNotNull { it.toIntOrNull() }
        val startH = startParts.getOrNull(0) ?: 8
        val startM = startParts.getOrNull(1) ?: 0

        val endParts = endStr.split(":").mapNotNull { it.toIntOrNull() }
        val endH = endParts.getOrNull(0) ?: 9
        val endM = endParts.getOrNull(1) ?: 0

        Triple(Pair(startH, startM), Pair(endH, endM), startStr to endStr)
    }

    var currentStep by remember { mutableStateOf(TimeStep.START_TIME) }

    var startHour by remember { mutableIntStateOf(parsedTimes.first.first) }
    var startMin by remember { mutableIntStateOf(parsedTimes.first.second) }

    var endHour by remember { mutableIntStateOf(parsedTimes.second.first) }
    var endMin by remember { mutableIntStateOf(parsedTimes.second.second) }

    val startPickerState = rememberTimePickerState(
        initialHour = startHour,
        initialMinute = startMin,
        is24Hour = false
    )

    val endPickerState = rememberTimePickerState(
        initialHour = endHour,
        initialMinute = endMin,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (currentStep == TimeStep.START_TIME) "Step 1: Select Start Time" else "Step 2: Select End Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val activeStartH = if (currentStep == TimeStep.START_TIME) startPickerState.hour else startHour
                    val activeStartM = if (currentStep == TimeStep.START_TIME) startPickerState.minute else startMin
                    val startFormatted = String.format("%02d:%02d", activeStartH, activeStartM)

                    val activeEndH = if (currentStep == TimeStep.END_TIME) endPickerState.hour else endHour
                    val activeEndM = if (currentStep == TimeStep.END_TIME) endPickerState.minute else endMin
                    val endFormatted = String.format("%02d:%02d", activeEndH, activeEndM)

                    FilterChip(
                        selected = currentStep == TimeStep.START_TIME,
                        onClick = { currentStep = TimeStep.START_TIME },
                        label = { Text("Start: $startFormatted", fontWeight = FontWeight.Bold) }
                    )

                    FilterChip(
                        selected = currentStep == TimeStep.END_TIME,
                        onClick = { currentStep = TimeStep.END_TIME },
                        label = { Text("End: $endFormatted", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "timePickerStep"
                ) { step ->
                    when (step) {
                        TimeStep.START_TIME -> TimePicker(state = startPickerState)
                        TimeStep.END_TIME -> TimePicker(state = endPickerState)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentStep == TimeStep.START_TIME) {
                        startHour = startPickerState.hour
                        startMin = startPickerState.minute

                        // Auto-set initial end time to +1 hour after start time if default
                        val defaultEndH = (startHour + 1) % 24
                        endHour = defaultEndH
                        endMin = startMin

                        // Automatically advance to End Time step
                        currentStep = TimeStep.END_TIME
                    } else {
                        endHour = endPickerState.hour
                        endMin = endPickerState.minute

                        val finalStart = String.format("%02d:%02d", startHour, startMin)
                        val finalEnd = String.format("%02d:%02d", endHour, endMin)
                        onConfirmTimeRange("$finalStart-$finalEnd")
                    }
                }
            ) {
                Text(
                    text = if (currentStep == TimeStep.START_TIME) "Next: Set End Time" else "Confirm Time Range",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (currentStep == TimeStep.END_TIME) {
                        currentStep = TimeStep.START_TIME
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (currentStep == TimeStep.END_TIME) "Back to Start" else "Cancel")
            }
        }
    )
}
