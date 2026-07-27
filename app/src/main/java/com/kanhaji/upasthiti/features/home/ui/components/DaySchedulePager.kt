package com.kanhaji.upasthiti.features.home.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent
import com.kanhaji.upasthiti.features.home.domain.model.TimetableData
import com.kanhaji.upasthiti.features.home.ui.HomeScreenModel
import com.kanhaji.upasthiti.screen.edit.EditClassScreen
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kanhaji.upasthiti.features.home.data.Subject

@Composable
fun DaySchedulePager(
    pagerState: PagerState,
    daysList: List<String>,
    timetableData: TimetableData,
    screenModel: HomeScreenModel,
    modifier: Modifier = Modifier,
    topPadding: Dp = 64.dp
) {
    val navigator = LocalNavigator.currentOrThrow
    var shiftingEvent by remember { mutableStateOf<Pair<ScheduleEvent, DayOfWeek>?>(null) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    shiftingEvent?.let { (event, dayOfWeek) ->
        val classEntity = remember(event, dayOfWeek) {
            ClassEntity(
                classId = event.id,
                dayOfWeek = dayOfWeek,
                time = event.time,
                subject = Subject(
                    displayName = event.course_code,
                    subjectId = event.course_code,
                    teacher = event.faculty_name ?: "",
                    teacherInitials = event.faculty_abbr ?: ""
                ),
                roomNo = event.location ?: "",
                attendanceStatus = null
            )
        }
        ClassShiftBottomSheet(
            classEntity = classEntity,
            currentDate = today,
            onDismiss = { shiftingEvent = null },
            onConfirmShift = { newDay, newTime, newLocation, effectiveDate ->
                screenModel.shiftClass(
                    classEntity = classEntity,
                    newDayOfWeek = newDay,
                    newTime = newTime,
                    newLocation = newLocation,
                    effectiveDate = effectiveDate
                )
                shiftingEvent = null
            }
        )
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) { pageIndex ->
        val day = daysList[pageIndex]
        val currentData = TimeTableManager.activeTimetableData ?: timetableData
        val dayEvents = TimeTableManager.getScheduleEventsForDay(day, currentData)

        if (dayEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topPadding + 16.dp, bottom = 90.dp, start = 24.dp, end = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No classes scheduled for $day.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedButton(
                        onClick = { navigator.push(EditClassScreen(initialDay = day)) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Class",
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Add Class to $day", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = topPadding + 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = dayEvents,
                    key = { index, event -> "${event.id}_${event.time}_$index" }
                ) { _, event ->
                    val courseInfo = timetableData.courses.find { it.code.equals(event.course_code, ignoreCase = true) }
                    ScheduleEventCard(
                        event = event,
                        courseInfo = courseInfo,
                        onEditClick = { ev -> navigator.push(EditClassScreen(event = ev)) },
                        onShiftClick = { ev ->
                            val parsedDay = runCatching { DayOfWeek.valueOf(day.uppercase()) }.getOrDefault(DayOfWeek.MONDAY)
                            shiftingEvent = Pair(ev, parsedDay)
                        }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = { navigator.push(EditClassScreen(initialDay = day)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Class"
                            )
                            Text(
                                text = "Add Class to $day",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
