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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.kanhaji.upastithi.features.home.domain.model.CourseInfo
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent

@Composable
fun EditClassDialog(
    event: ScheduleEvent,
    courseInfo: CourseInfo?,
    onSave: (updatedEvent: ScheduleEvent, updatedCourseName: String?) -> Unit,
    onDelete: (eventToDelete: ScheduleEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var time by remember { mutableStateOf(event.time) }
    var courseCode by remember { mutableStateOf(event.course_code) }
    var courseName by remember { mutableStateOf(courseInfo?.name ?: "") }
    var type by remember { mutableStateOf(if (event.type.equals("P", ignoreCase = true) || event.type.equals("Practical", ignoreCase = true)) "Practical" else "Lecture") }
    var location by remember { mutableStateOf(event.location ?: "") }
    var facultyName by remember { mutableStateOf(event.faculty_name ?: "") }
    var group by remember { mutableStateOf(event.group ?: "") }

    var showTypePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Class",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Edit Class Details",
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
                    val isLab = type.equals("Practical", ignoreCase = true)
                    val updated = event.copy(
                        time = time.trim(),
                        course_code = courseCode.trim(),
                        type = if (isLab) "P" else "L",
                        location = location.trim().ifEmpty { null },
                        faculty_name = facultyName.trim().ifEmpty { null },
                        group = group.trim().ifEmpty { null }
                    )
                    onSave(updated, courseName.trim().ifEmpty { null })
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onDelete(event) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Delete")
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
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
