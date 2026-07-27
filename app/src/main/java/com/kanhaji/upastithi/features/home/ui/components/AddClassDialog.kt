package com.kanhaji.upastithi.features.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.composables.RadioItem
import com.kanhaji.basics.composables.RadioSelectionDialog
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent

@Composable
fun AddClassDialog(
    dayName: String,
    onAdd: (newEvent: ScheduleEvent, courseName: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var time by remember { mutableStateOf("08:00-09:00") }
    var courseCode by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Lecture") }
    var location by remember { mutableStateOf("") }
    var facultyName by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }

    var showTypePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Class",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Add Class to $dayName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 08:00-09:00)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { newCode ->
                        courseCode = newCode
                        val matchingName = com.kanhaji.upastithi.data.TimeTableManager.getCourseName(newCode)
                        if (matchingName.isNotBlank() && !matchingName.equals(newCode, ignoreCase = true)) {
                            courseName = matchingName
                        }
                    },
                    label = { Text("Course Code (e.g. CS35101)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = courseName,
                    onValueChange = { newName ->
                        courseName = newName
                        val matchingCode = com.kanhaji.upastithi.data.TimeTableManager.getCourseCode(newName)
                        if (!matchingCode.isNullOrBlank()) {
                            courseCode = matchingCode
                        }
                    },
                    label = { Text("Course Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showTypePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Class Type",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Room / Location (e.g. GS8)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = facultyName,
                    onValueChange = { facultyName = it },
                    label = { Text("Faculty / Instructor Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text("Group / Batch (Optional, e.g. Group B)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (courseCode.isNotBlank()) {
                        val isLab = type.equals("Practical", ignoreCase = true)
                        val times = time.split("-").map { it.trim() }
                        val startTime = times.getOrNull(0) ?: "08:00"
                        val endTime = times.getOrNull(1) ?: "09:00"

                        val newEvent = ScheduleEvent(
                            day = dayName,
                            time = time.trim(),
                            course_code = courseCode.trim(),
                            type = if (isLab) "P" else "L",
                            start_time = startTime,
                            end_time = endTime,
                            location = location.trim().ifEmpty { null },
                            faculty_name = facultyName.trim().ifEmpty { null },
                            group = group.trim().ifEmpty { null }
                        )
                        onAdd(newEvent, courseName.trim().ifEmpty { null })
                    }
                },
                enabled = courseCode.isNotBlank() && time.isNotBlank()
            ) {
                Text("Add Class")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showTypePicker) {
        val options = listOf(
            RadioItem(label = "Lecture", onClick = { type = "Lecture" }),
            RadioItem(label = "Practical / Lab", onClick = { type = "Practical" })
        )

        RadioSelectionDialog(
            title = "Select Class Type",
            options = options,
            initialSelection = if (type.equals("Practical", ignoreCase = true)) 1 else 0,
            onConfirm = { showTypePicker = false },
            onDismiss = { showTypePicker = false }
        )
    }
}
