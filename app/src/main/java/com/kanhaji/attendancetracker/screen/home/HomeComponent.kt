package com.kanhaji.attendancetracker.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kanhaji.attendancetracker.screen.home.components.ClassAttendanceStepperDialog
import com.kanhaji.attendancetracker.util.getClasses
import io.github.boguszpawlowski.composecalendar.StaticCalendar
import io.github.boguszpawlowski.composecalendar.day.DefaultDay
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate

@Composable
fun HomeComponent(screenModel: HomeScreenModel) {
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedDayOfWeek by remember { mutableStateOf<DayOfWeek?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StaticCalendar(
            dayContent = { dayState ->
                DefaultDay(
                    state = dayState,
                    onClick = {
                        selectedDate = dayState.date.toKotlinLocalDate()
                        selectedDayOfWeek = dayState.date.dayOfWeek
                        showDateDialog = true
                    }
                )
            }
        )
    }

    if (showDateDialog && selectedDate != null && selectedDayOfWeek != null) {
        ClassAttendanceStepperDialog(
//            classes = screenModel.getClassesForDay(selectedDayOfWeek!!),
            classes = selectedDayOfWeek!!.getClasses(),
            screenModel = screenModel,
            date = selectedDate!!,
            dayOfWeek = selectedDayOfWeek!!,
        ) {
            showDateDialog = false
        }
    }
}