package com.kanhaji.upastithi.screen.edit

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.upastithi.data.TimeTableManager
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent
import kotlinx.coroutines.launch
import com.kanhaji.upastithi.screen.edit.components.EditClassActionButtons
import com.kanhaji.upastithi.screen.edit.components.EditClassTopAppBar
import com.kanhaji.upastithi.screen.edit.components.EditCourseInfoCard
import com.kanhaji.upastithi.screen.edit.components.EditScheduleTimingCard
import com.kanhaji.upastithi.screen.edit.components.EditTimePickerDialog
import com.kanhaji.upastithi.screen.edit.components.EditVenueFacultyCard

@Composable
fun EditClassComponent(
    event: ScheduleEvent? = null,
    initialDay: String = "Monday"
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val isEditMode = event != null

    val daysList = remember { listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday") }

    var selectedDay by remember { mutableStateOf(event?.day ?: initialDay) }
    var time by remember { mutableStateOf(event?.time ?: "08:00-09:00") }
    var courseCode by remember { mutableStateOf(event?.course_code ?: "") }

    val initialCourseName = remember(event) {
        event?.let { ev ->
            TimeTableManager.activeTimetableData?.courses?.find { it.code.equals(ev.course_code, ignoreCase = true) }?.name ?: ""
        } ?: ""
    }
    var courseName by remember { mutableStateOf(initialCourseName) }

    var isPractical by remember {
        mutableStateOf(event?.type?.equals("P", ignoreCase = true) == true || event?.type?.equals("Practical", ignoreCase = true) == true)
    }

    var location by remember { mutableStateOf(event?.location ?: "") }
    var facultyName by remember { mutableStateOf(event?.faculty_name ?: "") }
    var group by remember { mutableStateOf(event?.group ?: "") }

    var showClockPicker by remember { mutableStateOf(false) }

    val parsedTimes = remember(time) { time.split("-").map { it.trim() } }
    val currentStart = parsedTimes.getOrNull(0) ?: "08:00"
    val currentEnd = parsedTimes.getOrNull(1) ?: "09:00"

    val collidingEvent = remember(selectedDay, currentStart, currentEnd, event) {
        TimeTableManager.findCollidingEvent(
            day = selectedDay,
            startTimeStr = currentStart,
            endTimeStr = currentEnd,
            excludeEventId = event?.id
        )
    }

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val isSaveEnabled = courseCode.isNotBlank() && time.isNotBlank() && collidingEvent == null

    fun performSave() {
        if (isSaveEnabled) {
            val times = time.split("-").map { it.trim() }
            val startTime = times.getOrNull(0) ?: "08:00"
            val endTime = times.getOrNull(1) ?: "09:00"

            val updatedEvent = ScheduleEvent(
                id = event?.id ?: java.util.UUID.randomUUID().toString(),
                day = selectedDay,
                time = time.trim(),
                start_time = startTime,
                end_time = endTime,
                course_code = courseCode.trim(),
                type = if (isPractical) "P" else "L",
                location = location.trim().ifEmpty { null },
                faculty_name = facultyName.trim().ifEmpty { null },
                group = group.trim().ifEmpty { null }
            )

            coroutineScope.launch {
                val repository = com.kanhaji.upastithi.features.home.data.repository.TimetableRepositoryImpl()
                if (isEditMode && event != null) {
                    repository.updateCustomEvent(event, updatedEvent, courseName.trim().ifEmpty { null })
                } else {
                    repository.saveCustomEvent(updatedEvent, courseName.trim().ifEmpty { null })
                }
                navigator.pop()
            }
        }
    }

    fun performDelete() {
        if (event != null) {
            coroutineScope.launch {
                val repository = com.kanhaji.upastithi.features.home.data.repository.TimetableRepositoryImpl()
                repository.deleteCustomEvent(event)
                navigator.pop()
            }
        }
    }

    Scaffold(
        topBar = {
            EditClassTopAppBar(
                isEditMode = isEditMode,
                onBackClick = { navigator.pop() },
                onSaveClick = { performSave() },
                isSaveEnabled = isSaveEnabled
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Schedule & Timing Component Card
            EditScheduleTimingCard(
                daysList = daysList,
                selectedDay = selectedDay,
                onDaySelected = { selectedDay = it },
                time = time,
                onOpenClockPicker = { showClockPicker = true },
                collidingEvent = collidingEvent
            )

            // 2. Course Information Component Card
            EditCourseInfoCard(
                courseCode = courseCode,
                onCourseCodeChanged = { courseCode = it },
                courseName = courseName,
                onCourseNameChanged = { courseName = it },
                isPractical = isPractical,
                onPracticalToggled = { isPractical = it }
            )

            // 3. Venue & Faculty Component Card
            EditVenueFacultyCard(
                location = location,
                onLocationChanged = { location = it },
                facultyName = facultyName,
                onFacultyNameChanged = { facultyName = it },
                group = group,
                onGroupChanged = { group = it }
            )

            // 4. Action Buttons Component
            EditClassActionButtons(
                isEditMode = isEditMode,
                isSaveEnabled = isSaveEnabled,
                onSaveClick = { performSave() },
                onDeleteClick = { performDelete() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showClockPicker) {
        EditTimePickerDialog(
            initialTimeRange = time,
            onConfirmTimeRange = { newTimeRange ->
                time = newTimeRange
                showClockPicker = false
            },
            onDismiss = { showClockPicker = false }
        )
    }
}
