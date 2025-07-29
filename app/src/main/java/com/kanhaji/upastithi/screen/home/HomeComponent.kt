package com.kanhaji.upastithi.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.composable.GenericLazyColumn
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.screen.home.components.ClassAttendanceStepperDialog
import com.kanhaji.upastithi.screen.home.components.SubjectCardSingle
import com.kanhaji.upastithi.util.getClasses
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@Composable
fun HomeComponent(screenModel: HomeScreenModel) {
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedDayOfWeek by remember { mutableStateOf<DayOfWeek?>(null) }

    var refreshKey by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
//        StaticCalendar(
//            dayContent = { dayState ->
//                DefaultDay(
//                    state = dayState,
//                    onClick = {
//                        selectedDate = dayState.date.toKotlinLocalDate()
//                        selectedDayOfWeek = dayState.date.dayOfWeek
//                        showDateDialog = true
//                    }
//                )
//            }
//        )
        Text(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
    }

    if (showDateDialog && selectedDate != null && selectedDayOfWeek != null) {
        ClassAttendanceStepperDialog(
            classes = selectedDayOfWeek!!.getClasses(),
            screenModel = screenModel,
            date = selectedDate!!,
            dayOfWeek = selectedDayOfWeek!!,
        ) {
            showDateDialog = false
            refreshKey++
        }
    }
}