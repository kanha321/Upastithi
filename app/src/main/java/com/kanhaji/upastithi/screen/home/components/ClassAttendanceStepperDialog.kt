package com.kanhaji.upastithi.screen.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.composable.AttendanceItem
import com.kanhaji.upastithi.composable.GenericLazyColumn
import com.kanhaji.upastithi.composable.KRadioSelector
import com.kanhaji.upastithi.data.attendance.AttendanceStatus
import com.kanhaji.upastithi.entity.ClassEntity
import com.kanhaji.upastithi.screen.home.HomeScreenModel
import com.kanhaji.upastithi.util.KToast
import com.kanhaji.upastithi.util.noClassesMessages
import io.github.boguszpawlowski.composecalendar.kotlinxDateTime.now
import kotlinx.datetime.LocalDate

@Composable
fun ClassAttendanceStepperDialog(
    classes: List<ClassEntity>,
    screenModel: HomeScreenModel,
    date: LocalDate,
    onDismiss: () -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedClass by remember { mutableStateOf<ClassEntity?>(null) }
    var selectedAttendance by remember { mutableStateOf<AttendanceStatus?>(null) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 0 && classes.isNotEmpty()) "Select Class" else if(step == 0) "(❁´◡`❁)" else "Select Attendance Status"
            )
        },
        text = {
            if (classes.isEmpty()) {
                Text(noClassesMessages.random())
                return@AlertDialog
            }
            LaunchedEffect(selectedAttendance) {
                println("LaunchedEffect: classes: $classes")
                println("LaunchedEffect: Attendances are ${screenModel.getAttendanceForDate(date)}")
                println("LaunchedEffect: The color is " + selectedAttendance?.color.toString())
            }
            when (step) {
                0 -> {
                    // Step 1: Classes list
                    // First, get the list of saved attendances for the selected date ONCE.
                    val attendanceForDate = screenModel.getAttendanceForDate(date)
                    val defaultColor = MaterialTheme.colorScheme.surface
                    val defaultBorder = MaterialTheme.colorScheme.primary

                    GenericLazyColumn(
                        itemCount = classes.size,
                        listState = rememberLazyListState(),
                        itemSpacing = 2.dp,

                        // *** THIS IS THE IMPORTANT PART ***
                        // We are now using our new `containerColor` parameter.
                        // For each item in the list (at a given `index`), this code will run.
                        containerColor = { index ->
                            // This logic asks our model for the correct color.
                            screenModel.getAttendanceColor(
                                classEntity = classes[index],
                                attendancesForDate = attendanceForDate,
                                defaultColor = defaultColor
                            )
                        },

                        border = { index ->
                            BorderStroke(
                                2.dp, screenModel.getAttendanceColor(
                                    classEntity = classes[index],
                                    attendancesForDate = attendanceForDate,
                                    defaultColor = defaultBorder
                                )
                            )
                        },

                        onItemClick = { index ->

                            if (date > LocalDate.now()) {
                                KToast.show(context, "Cannot mark attendance for future dates.")
                            } else {
                                selectedClass = classes[index]
                                val classAttendance = screenModel.getAttendanceForClass(
                                    date = date,
                                    time = classes[index].time
                                )
                                selectedAttendance = classAttendance?.attendanceStatus
                                step = 1
                            }
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
                                icon = Icons.Default.CheckCircle,
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.ABSENT,
                                icon = Icons.Default.Cancel,
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.PROXY,
                                icon = Icons.Default.PersonAdd,
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.LEAVE,
                                icon = Icons.Default.FlightTakeoff,
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.HOLIDAY,
                                icon = Icons.Default.Celebration,
                            ),
                            AttendanceItem(
                                status = AttendanceStatus.CANCELLED,
                                icon = Icons.Default.Block,
                            )
                        ),
                        initialSelection = selectedAttendance?.displayName,
                    ) { selected ->
                        selectedAttendance = selected?.status
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
                        screenModel.saveAttendance(
                            classEntity = selectedClass!!,
                            attendanceStatus = selectedAttendance,
                            date
                        )
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
