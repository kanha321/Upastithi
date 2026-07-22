package com.kanhaji.upastithi.features.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PersonAdd
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
import com.kanhaji.upastithi.core.designsystem.components.AttendanceItem
import com.kanhaji.upastithi.core.designsystem.components.GenericLazyColumn
import com.kanhaji.upastithi.core.designsystem.components.KRadioSelector
import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.domain.model.ClassEntity
import com.kanhaji.upastithi.features.home.ui.HomeScreenModel
import com.kanhaji.upastithi.util.KToast
import com.kanhaji.upastithi.util.UpasthitiUtils
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
                text = if (step == 0 && classes.isNotEmpty()) "Select Class" else if (step == 0) "(❁´◡`❁)" else "Select Attendance Status"
            )
        },
        text = {
            if (classes.isEmpty()) {
                Text(text = UpasthitiUtils.noClassesMessages.random())
                return@AlertDialog
            }
            LaunchedEffect(selectedAttendance) {
                println("LaunchedEffect: classes: $classes")
                println("LaunchedEffect: Attendances are ${screenModel.getAttendanceForDate(date)}")
                println("LaunchedEffect: The color is " + selectedAttendance?.color.toString())
            }
            when (step) {
                0 -> {
                    val attendanceForDate = screenModel.getAttendanceForDate(date)
                    val defaultColor = MaterialTheme.colorScheme.surface
                    val defaultBorder = MaterialTheme.colorScheme.primary

                    GenericLazyColumn(
                        itemCount = classes.size,
                        listState = rememberLazyListState(),
                        itemSpacing = 2.dp,
                        containerColor = { index ->
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
                            println("Please select a class and attendance status.")
                            return@TextButton
                        }
                        screenModel.saveAttendance(
                            classEntity = selectedClass!!,
                            attendanceStatus = selectedAttendance,
                            date
                        )
                        println("Selected attendance status onDone: $selectedAttendance")
                        step = 0
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
