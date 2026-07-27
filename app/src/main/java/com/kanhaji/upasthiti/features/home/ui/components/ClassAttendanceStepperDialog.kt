package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kanhaji.upasthiti.core.designsystem.components.AttendanceItem
import com.kanhaji.upasthiti.core.designsystem.components.GenericLazyColumn
import com.kanhaji.upasthiti.core.designsystem.components.KRadioSelector
import com.kanhaji.upasthiti.features.home.data.AttendanceStatus
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.ui.HomeScreenModel
import com.kanhaji.upasthiti.util.KToast
import com.kanhaji.upasthiti.util.UpasthitiUtils
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.IconButton
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

    val dateFormatted = remember(date) {
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "${date.dayOfMonth} $monthName ${date.year}"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Title & Date Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (step == 0) "Mark Attendance" else "Select Status",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val subtitleText = remember(step, dateFormatted, selectedClass) {
                            if (step == 0) dateFormatted
                            else {
                                val name = selectedClass?.subject?.displayName ?: dateFormatted
                                val grp = selectedClass?.group
                                if (!grp.isNullOrBlank()) "$name • Group $grp" else name
                            }
                        }
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Main Content Body
                if (classes.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = UpasthitiUtils.noClassesMessages.random(),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "stepSlideTransition"
                    ) { targetStep ->
                        when (targetStep) {
                            0 -> {
                                val attendanceForDate = screenModel.getAttendanceForDate(date)
                                val labContainerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                val defaultContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                val labBorderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                val defaultBorderColor = MaterialTheme.colorScheme.outlineVariant

                                GenericLazyColumn(
                                    itemCount = classes.size,
                                    listState = rememberLazyListState(),
                                    columnPadding = 0.dp,
                                    itemSpacing = 4.dp,
                                    containerColor = { index ->
                                        val isLab = classes[index].subject.displayName.contains("Lab", ignoreCase = true) ||
                                                    classes[index].subject.displayName.contains("Practical", ignoreCase = true)
                                        val fallbackColor = if (isLab) labContainerColor else defaultContainerColor
                                        screenModel.getAttendanceColor(
                                            classEntity = classes[index],
                                            attendancesForDate = attendanceForDate,
                                            defaultColor = fallbackColor
                                        )
                                    },
                                    border = { index ->
                                        val isLab = classes[index].subject.displayName.contains("Lab", ignoreCase = true) ||
                                                    classes[index].subject.displayName.contains("Practical", ignoreCase = true)
                                        val fallbackBorder = if (isLab) labBorderColor else defaultBorderColor
                                        BorderStroke(
                                            1.dp, screenModel.getAttendanceColor(
                                                classEntity = classes[index],
                                                attendancesForDate = attendanceForDate,
                                                defaultColor = fallbackBorder
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
                                    ClassCardSingle(classEntity = classes[index])
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
                                    gridColumns = 2,
                                ) { selected ->
                                    selectedAttendance = selected?.status
                                }
                            }
                        }
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step == 0) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Close")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (selectedClass != null) {
                                    screenModel.saveAttendance(
                                        classEntity = selectedClass!!,
                                        attendanceStatus = selectedAttendance,
                                        date
                                    )
                                    step = 0
                                    selectedClass = null
                                    selectedAttendance = null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Save")
                        }

                        OutlinedButton(
                            onClick = {
                                if (selectedClass != null) {
                                    screenModel.saveAttendance(
                                        classEntity = selectedClass!!,
                                        attendanceStatus = selectedAttendance,
                                        date
                                    )
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}
