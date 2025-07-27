package com.kanhaji.attendancetracker.screen.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kanhaji.attendancetracker.composable.AttendanceItem
import com.kanhaji.attendancetracker.composable.GenericLazyColumn
import com.kanhaji.attendancetracker.composable.KRadioSelector
import com.kanhaji.attendancetracker.data.attendance.AttendanceStatus
import com.kanhaji.attendancetracker.entity.AttendanceEntity
import com.kanhaji.attendancetracker.entity.ClassEntity
import com.kanhaji.attendancetracker.screen.home.HomeScreenModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
fun ClassAttendanceStepperDialog(
    classes: List<ClassEntity>,
    screenModel: HomeScreenModel,
    date: LocalDate,
    dayOfWeek: DayOfWeek,
    attendanceForDate: List<AttendanceEntity> = screenModel.getAttendanceForDate(date),
    onDismiss: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedClass by remember { mutableStateOf<ClassEntity?>(null) }
    var selectedAttendance by remember { mutableStateOf<AttendanceStatus?>(null) }
    var attendanceForTime by remember{ mutableStateOf<AttendanceEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 0) "Select Class" else "Select Attendance Status"
            )
        },
        text = {
            if (classes.isEmpty()) {
                // Show a fun and friendly message if no classes are available
                Text("It's a quiet day! No classes available for attendance.")
                return@AlertDialog
            }
            when (step) {
                0 -> {
                    // Step 1: Classes list
                    GenericLazyColumn(
                        itemCount = classes.size,
                        listState = rememberLazyListState(),
                        itemSpacing = 2.dp,
                        // ... inside when(step) { 0 -> ... }
                        onItemClick = { index ->
                            selectedClass = classes[index]

                            // Fetch the existing attendance record for the selected class
                            val classAttendance = screenModel.getAttendanceForClass(date = date, time = classes[index].time)

                            // *** THIS IS THE KEY CHANGE ***
                            // Set the initial selection state based on the fetched record.
                            selectedAttendance = classAttendance?.attendanceStatus
                            println("Selected attendance status itemClick: $selectedAttendance")

                            // Now, move to the next step
                            step = 1
                            println("Attendance for class ${classes[index].subject} at ${classes[index].time}: $classAttendance")
                        }
                    ) { index ->
                        ClassCardSingle(classes[index])
                    }
                }

                1 -> {
                    // Step 2: Attendance selection
                    KRadioSelector(
                        items = listOf(
                            AttendanceItem(
                                status = AttendanceStatus.PRESENT,
                                icon = Icons.Default.Star,
                                highLightColor = Color(0xFF24AB27) // Green for present
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.ABSENT,
                                icon = Icons.Default.Star,
                                highLightColor = Color(0xFFAB2424) // Red for absent
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.PROXY,
                                icon = Icons.Default.Star,
                                highLightColor = Color(0xFFFFA500) // Orange for proxy
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.LEAVE,
                                icon = Icons.Default.Star,
                                highLightColor = Color(0xFF2552C2) // Blue for leave
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.HOLIDAY,
                                icon = Icons.Default.Star,
                                highLightColor = Color(0xFF8B4513) // Brown for holiday
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.CANCELLED,
                                icon = Icons.Default.Star,
                                highLightColor = Color(0xFF888888) // Grey for cancelled
                            )
                        ),
                        initialSelection = selectedAttendance?.displayName,
                    ) { selected -> selectedAttendance = selected?.status
                        // Handle selection if immediate logic is needed
                        println("Selected attendance status onChange: $selectedAttendance")
                    }
                }
            }
        },
        confirmButton = {
            Row {
                if (step > 0) {
                    TextButton(onClick = {
                        step--
//                        screenModel.saveAttendance(classEntity = selectedClass!!, attendanceStatus = selectedAttendance, date, dayOfWeek)
                    }) {
                        Text("Back")
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    if (step == 0) {
                        onDismiss()
                    } else {
                        if (selectedClass == null) {
                            // Show error or handle invalid state
                            println("Please select a class and attendance status.")
                            return@TextButton
                        }
                        screenModel.saveAttendance(classEntity = selectedClass!!, attendanceStatus = selectedAttendance, date, dayOfWeek)
                        println("Selected attendance status onDone: $selectedAttendance")
                        step = 0 // Reset to initial step after saving
                    }
                }) {
                    Text(
                        text = if (step == 0) "Done" else "Save",
                    )
                }
            }
        }
    )
}
