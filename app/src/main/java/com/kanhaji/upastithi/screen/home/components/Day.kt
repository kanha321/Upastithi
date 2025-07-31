package com.kanhaji.upastithi.screen.home.components

import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.screen.home.HomeScreenModel
import com.kanhaji.upastithi.util.KToast
import com.kanhaji.upastithi.util.getClasses
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun Day(day: CalendarDay, screenModel: HomeScreenModel) {
    val context = LocalContext.current

    var showDateDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedDayOfWeek by remember { mutableStateOf<DayOfWeek?>(null) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }


    Box(
        modifier = Modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(
            onClick = {
                if (day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() <= today) {
                    selectedDate = day.date.toKotlinLocalDate()
                    selectedDayOfWeek = day.date.dayOfWeek
                    showDateDialog = true
                    return@OutlinedCard
                }
                if (day.date.toKotlinLocalDate() > today && day.position == DayPosition.MonthDate) {
                    KToast.show(context, "Cannot add future attendance", Toast.LENGTH_SHORT)
                    return@OutlinedCard
                }
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
            border = if (day.position == DayPosition.MonthDate && day.date.toKotlinLocalDate() < today) {
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
                    // Date number
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
                    MultiDotIndicator(
                        date = day.date.toKotlinLocalDate(),
                        allAttendances = screenModel.attendanceByDate[day.date.toKotlinLocalDate()] ?: emptyList(),
                        screenModel = screenModel
                    )
                }
            }
        }
    }

    if (showDateDialog && selectedDate != null && selectedDayOfWeek != null) {
        ClassAttendanceStepperDialog(
            classes = selectedDayOfWeek!!.getClasses(),
            screenModel = screenModel,
            date = selectedDate!!,
        ) {
            showDateDialog = false
        }
    }
}
