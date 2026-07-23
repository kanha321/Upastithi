// Kotlin
package com.kanhaji.upastithi.screen.home.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.util.Updater
import com.kanhaji.upastithi.screen.home.HomeScreenModel
import com.kanhaji.upastithi.screen.home.components.Day
import com.kanhaji.upastithi.screen.home.components.DaysOfWeekTitle
import com.kanhaji.upastithi.screen.home.components.InfoNoteCard
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarSection(
    screenModel: HomeScreenModel
) {
    val scope = rememberCoroutineScope()

    // Wider range for smoother navigation.
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.SUNDAY
    )

    val visibleMonth = state.firstVisibleMonth.yearMonth
    val monthTitle = remember(visibleMonth) {
        val monthName = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "$monthName ${visibleMonth.year}"
    }

    val canGoPrev = visibleMonth.isAfter(startMonth)
    val canGoNext = visibleMonth.isBefore(endMonth)



    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (canGoPrev) {
                                scope.launch { state.animateScrollToMonth(visibleMonth.minusMonths(1)) }
                            }
                        },
                        enabled = canGoPrev
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronLeft,
                            contentDescription = "Previous month"
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(targetState = monthTitle, label = "MonthTitle") { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { scope.launch { state.animateScrollToMonth(currentMonth) } },
                            label = { Text("Today") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Today,
                                    contentDescription = "Today"
                                )
                            }
                        )
                        IconButton(
                            onClick = {
                                if (canGoNext) {
                                    scope.launch {
                                        state.animateScrollToMonth(
                                            visibleMonth.plusMonths(
                                                1
                                            )
                                        )
                                    }
                                }
                            },
                            enabled = canGoNext
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = "Next month"
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                HorizontalCalendar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    state = state,
                    dayContent = { day ->
                        Day(day = day, screenModel = screenModel)
                    },
                    monthHeader = {
                        DaysOfWeekTitle()
                    }
                )

                Spacer(Modifier.height(8.dp))

//                Text(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp),
//                    text = "Tap a date to view timetable or add attendance",
//                    style = MaterialTheme.typography.bodySmall,
//                    textAlign = TextAlign.Center,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    fontSize = 12.sp
//                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        InfoNoteCard(
            text = "Tap a date to view timetable or add attendance"
        )
    }
}