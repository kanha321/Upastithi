package com.kanhaji.upastithi.screen.home

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanhaji.upastithi.composable.GenericLazyColumn
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.data.attendance.AttendanceStatus
import com.kanhaji.upastithi.entity.AttendanceEntity
import com.kanhaji.upastithi.isDarkMode
import com.kanhaji.upastithi.screen.home.components.ClassAttendanceStepperDialog
import com.kanhaji.upastithi.screen.home.components.SubjectCardSingle
import com.kanhaji.upastithi.util.KToast
import com.kanhaji.upastithi.util.getClasses
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.todayIn
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.ExperimentalTime

@Composable
fun HomeComponent2(
    screenModel: HomeScreenModel
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(5) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(0) } // Adjust as needed
    val daysOfWeek = remember { daysOfWeek() }

    var refreshKey by remember { mutableIntStateOf(0) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    val visibleMonth = state.firstVisibleMonth.yearMonth
    val monthName = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = visibleMonth.year


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$monthName $year",
            fontSize = 32.sp,
            modifier = Modifier
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        HorizontalCalendar(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            state = state,
            dayContent = { Day(day = it, screenModel = screenModel) },
            monthHeader = {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            }
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Click on date to view time table or add attendance record",
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier.padding(16.dp)
        ) {
            GenericLazyColumn(
                key = refreshKey,
                itemCount = Subject.getAllSubjects().size,
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                SubjectCardSingle(screenModel, Subject.getAllSubjects()[it], refreshKey)
            }
        }
        Switch(
            checked = isDarkMode,
            onCheckedChange = {
                isDarkMode = it
            },
        )
    }
}

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
            dayOfWeek = selectedDayOfWeek!!,
        ) {
            showDateDialog = false
        }
    }
}

@Composable
fun getDotColorForDate(date: LocalDate, screenModel: HomeScreenModel): Color {
    val attendanceList = screenModel.attendanceByDate[date] ?: return Color.Transparent

    return when {
        attendanceList.any { it.attendanceStatus == AttendanceStatus.ABSENT } -> AttendanceStatus.ABSENT.color
        attendanceList.any { it.attendanceStatus == AttendanceStatus.PRESENT } -> AttendanceStatus.PRESENT.color
        attendanceList.any { it.attendanceStatus == AttendanceStatus.PROXY } -> AttendanceStatus.PROXY.color
        attendanceList.any { it.attendanceStatus == AttendanceStatus.LEAVE } -> AttendanceStatus.LEAVE.color
        attendanceList.any { it.attendanceStatus == AttendanceStatus.CANCELLED } -> AttendanceStatus.CANCELLED.color
        attendanceList.any { it.attendanceStatus == AttendanceStatus.HOLIDAY } -> AttendanceStatus.HOLIDAY.color
        else -> Color.Transparent
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { dow ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = dow.name.take(3), style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MultiDotIndicator(date: LocalDate, allAttendances: List<AttendanceEntity>, screenModel: HomeScreenModel) {
    val dotColors = screenModel.getAttendanceDotsForDate(date, allAttendances)

    if (dotColors.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            dotColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
