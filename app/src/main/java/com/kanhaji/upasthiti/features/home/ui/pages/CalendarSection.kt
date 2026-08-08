package com.kanhaji.upasthiti.features.home.ui.pages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanhaji.upasthiti.features.home.ui.HomeScreenModel
import com.kanhaji.upasthiti.features.home.ui.components.Day
import com.kanhaji.upasthiti.features.home.ui.components.InfoNoteCard
import com.kanhaji.upasthiti.features.home.ui.components.WeekName
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

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(12) } // Adjust as needed

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
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .animateContentSize()
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
                        modifier = Modifier.weight(1f),
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
                                        state.animateScrollToMonth(visibleMonth.plusMonths(1))
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

                HorizontalCalendar(
                    modifier = Modifier.animateContentSize(),
                    state = state,
                    dayContent = { day ->
                        Day(day = day, screenModel = screenModel)
                    },
                    monthHeader = { month ->
                        WeekName(daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek })
                    }
                )
            }
        }
        InfoNoteCard(
            text = "Tap the ? icon in the top app bar anytime for help",
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 90.dp)
        )
    }
}
