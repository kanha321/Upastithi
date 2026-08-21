package com.kanhaji.upasthiti.features.home.ui.components

import android.widget.Toast
import com.kanhaji.upasthiti.data.TimeTableManager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.ui.HomeScreenModel
import com.kanhaji.upasthiti.util.getClasses
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalMaterial3Api::class)
@Composable
fun Day(day: CalendarDay, screenModel: HomeScreenModel) {
    val context = LocalContext.current

    var showUploadPromptDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showSaturdayDialog by remember { mutableStateOf(false) }
    var showWeekendDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedDayOfWeek by remember { mutableStateOf<DayOfWeek?>(null) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    Box(
        modifier = Modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null) {
            OutlinedCard(
                onClick = {
                    if (TimeTableManager.activeTimetableData == null) {
                        showUploadPromptDialog = true
                        return@OutlinedCard
                    }
                    val dateKotlin = day.date.toKotlinLocalDate()
                    selectedDate = dateKotlin
                    selectedDayOfWeek = day.date.dayOfWeek

                    val resolvedSaturday = com.kanhaji.upasthiti.features.home.data.SaturdayScheduleManager.resolveDayOfWeek(dateKotlin)
                    if (day.date.dayOfWeek == java.time.DayOfWeek.SATURDAY && resolvedSaturday != null) {
                        showSaturdayDialog = true
                    } else if (day.date.dayOfWeek == java.time.DayOfWeek.SATURDAY || day.date.dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                        showWeekendDialog = true
                    } else {
                        showDateDialog = true
                    }
                    return@OutlinedCard
                },
                shape = if (day.date.toKotlinLocalDate() == today) {
                    RoundedCornerShape(100.dp)
                } else {
                    RoundedCornerShape(8.dp)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                elevation = if (day.position == DayPosition.MonthDate) {
                    CardDefaults.elevatedCardElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp,
                        hoveredElevation = 3.dp
                    )
                } else {
                    CardDefaults.elevatedCardElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    )
                },
                border = if (day.date.dayOfWeek == DayOfWeek.SUNDAY && day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() <= today) {
                    BorderStroke(
                        color = Color.Red.copy(0.5f),
                        width = 1.dp
                    )
                } else if (day.date.dayOfWeek == DayOfWeek.SATURDAY && day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() <= today) {
                    BorderStroke(
                        color = Color.Blue.copy(0.5f),
                        width = 1.dp
                    )
                } else if (day.date.dayOfWeek == DayOfWeek.SUNDAY && day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() > today) {
                    BorderStroke(
                        color = Color.Red.copy(0.3f),
                        width = 1.dp
                    )
                } else if (day.date.dayOfWeek == DayOfWeek.SATURDAY && day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() > today) {
                    BorderStroke(
                        color = Color.Blue.copy(0.3f),
                        width = 1.dp
                    )
                } else if (day.date.toKotlinLocalDate() == today) {
                    BorderStroke(
                        color = MaterialTheme.colorScheme.primary,
                        width = 2.dp
                    )
                } else if (day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() < today) {
                    BorderStroke(
                        color = MaterialTheme.colorScheme.primary,
                        width = 1.dp
                    )
                } else if (day.date.toKotlinLocalDate() > today && day.position == DayPosition.MonthDate) {
                    BorderStroke(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        width = 1.dp
                    )
                } else {
                    BorderStroke(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        width = 0.dp
                    )
                },
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (day.date.toKotlinLocalDate() == today) {
                        MaterialTheme.colorScheme.primary
                    } else if (day.position == DayPosition.MonthDate) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    },
                    contentColor = if (day.date.toKotlinLocalDate() == today) {
                        MaterialTheme.colorScheme.onPrimary
                    } else if (day.position == DayPosition.MonthDate) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    }
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            color = if (day.date.toKotlinLocalDate() == today) {
                                MaterialTheme.colorScheme.surface
                            } else if (day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() <= today) {
                                MaterialTheme.colorScheme.onSurface
                            } else if (day.date.toKotlinLocalDate() > today && day.position == DayPosition.MonthDate) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                        val isCurrentMonth = day.position == DayPosition.MonthDate
                        val dateKotlin = day.date.toKotlinLocalDate()
                        MultiDotIndicator(
                            date = dateKotlin,
                            allAttendances = screenModel.attendanceByDate[dateKotlin] ?: emptyList(),
                            isToday = dateKotlin == today,
                            isGrayedOut = !isCurrentMonth,
                            isFutureDate = isCurrentMonth && dateKotlin > today
                        )
                    }
                }
            }
        }
    }

    var dialogClasses by remember(selectedDate) { mutableStateOf<List<ClassEntity>?>(null) }

    androidx.compose.runtime.LaunchedEffect(showDateDialog, showSaturdayDialog, selectedDate) {
        if ((showDateDialog || showSaturdayDialog) && selectedDate != null) {
            dialogClasses = screenModel.getClassesForDateUseCase(selectedDate!!)
        }
    }

    if (showUploadPromptDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUploadPromptDialog = false },
            title = {
                Text(
                    text = "Upload Timetable",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Please upload a timetable PDF in the Timetable tab to view your class schedule and mark attendance.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showUploadPromptDialog = false }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDateDialog && selectedDate != null && dialogClasses != null) {
        ClassAttendanceStepperDialog(
            classes = dialogClasses!!,
            screenModel = screenModel,
            date = selectedDate!!,
        ) {
            showDateDialog = false
            dialogClasses = null
        }
    }

    if (showSaturdayDialog && selectedDate != null && dialogClasses != null) {
        SaturdayAttendanceStepperDialog(
            initialClasses = dialogClasses!!,
            screenModel = screenModel,
            date = selectedDate!!,
            onDismiss = {
                showSaturdayDialog = false
                dialogClasses = null
            }
        )
    }

    if (showWeekendDialog && selectedDate != null) {
        WeekendHolidayDialog(
            date = selectedDate!!,
            onDismiss = {
                showWeekendDialog = false
            }
        )
    }
}
