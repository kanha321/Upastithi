package com.kanhaji.upastithi.screen.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanhaji.upastithi.composable.GenericLazyColumn
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.screen.home.components.Day
import com.kanhaji.upastithi.screen.home.components.DaysOfWeekTitle
import com.kanhaji.upastithi.screen.home.components.SubjectCardSingle
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DayOfWeek
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

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
//        Switch(
//            checked = isDarkMode,
//            onCheckedChange = {
//                isDarkMode = it
//            },
//        )
    }
}

