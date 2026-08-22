package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.domain.model.ClassEntity
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent
import com.kanhaji.upasthiti.screen.edit.components.EditTimePickerDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassShiftBottomSheet(
    classEntity: ClassEntity,
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirmShift: (newDay: DayOfWeek, newTime: String, newLocation: String, effectiveDate: LocalDate) -> Unit,
    onConfirmSwap: (targetEvent: ScheduleEvent, targetDay: DayOfWeek, effectiveDate: LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    // Shift Mode State
    var selectedDay by remember { mutableStateOf(classEntity.dayOfWeek) }
    var timeInput by remember { mutableStateOf(classEntity.time) }
    var locationInput by remember { mutableStateOf(classEntity.roomNo) }
    var showClockPicker by remember { mutableStateOf(false) }

    // Swap Mode State
    var selectedSwapTargetDay by remember { mutableStateOf(classEntity.dayOfWeek) }
    var selectedSwapTargetEvent by remember { mutableStateOf<ScheduleEvent?>(null) }

    val effectiveDate = remember { currentDate }

    val parsedTimes = remember(timeInput) { timeInput.split("-").map { it.trim() } }
    val currentStart = parsedTimes.getOrNull(0) ?: "08:00"
    val currentEnd = parsedTimes.getOrNull(1) ?: "09:00"
    val dayName = remember(selectedDay) { selectedDay.name.lowercase().replaceFirstChar { it.uppercase() } }

    val collidingEvent = remember(dayName, currentStart, currentEnd, classEntity) {
        TimeTableManager.findCollidingEvent(
            day = dayName,
            startTimeStr = currentStart,
            endTimeStr = currentEnd,
            excludeEventId = classEntity.classId
        )
    }

    if (showClockPicker) {
        EditTimePickerDialog(
            initialTimeRange = timeInput,
            onConfirmTimeRange = { newTime ->
                timeInput = newTime
                showClockPicker = false
            },
            onDismiss = { showClockPicker = false }
        )
    }

    val scrollState = rememberScrollState()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(imeBottomPadding) {
        if (imeBottomPadding > 0.dp) {
            delay(150)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = { WindowInsets.ime },
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .animateContentSize(
                    animationSpec = tween(durationMillis = 250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Shift / Swap Class",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subjectDisplayName = remember(classEntity) {
                    val name = TimeTableManager.getCourseName(classEntity.subject.subjectId)
                    if (name.isNotBlank()) name else classEntity.subject.displayName
                }
                Text(
                    text = "$subjectDisplayName • ${classEntity.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Spring Animated Mode Switcher
            RescheduleTabRow(
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(page)
                    }
                }
            )

            // Swipeable HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                if (page == 0) {
                    // Page 0: Shift Mode
                    ShiftClassSection(
                        selectedDay = selectedDay,
                        onDaySelected = { selectedDay = it },
                        timeInput = timeInput,
                        onTimeClicked = { showClockPicker = true },
                        locationInput = locationInput,
                        onLocationChanged = { locationInput = it },
                        collidingEventName = collidingEvent?.let { ev ->
                            val name = TimeTableManager.getCourseName(ev.course_code)
                            if (name.isNotBlank()) name else ev.course_code
                        }
                    )
                } else {
                    // Page 1: Swap Mode
                    SwapClassSection(
                        sourceClass = classEntity,
                        selectedTargetDay = selectedSwapTargetDay,
                        onTargetDayChanged = {
                            selectedSwapTargetDay = it
                            selectedSwapTargetEvent = null // Reset selected event when day changes
                        },
                        selectedTargetEvent = selectedSwapTargetEvent,
                        onTargetEventSelected = { selectedSwapTargetEvent = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }

                if (pagerState.currentPage == 0) {
                    // Confirm Shift Button
                    Button(
                        onClick = {
                            onConfirmShift(selectedDay, timeInput, locationInput, effectiveDate)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Shift",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Confirm Shift", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Confirm Swap Button
                    Button(
                        onClick = {
                            selectedSwapTargetEvent?.let { targetEvent ->
                                onConfirmSwap(targetEvent, selectedSwapTargetDay, effectiveDate)
                            }
                        },
                        enabled = selectedSwapTargetEvent != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap Classes",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Confirm Swap", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
