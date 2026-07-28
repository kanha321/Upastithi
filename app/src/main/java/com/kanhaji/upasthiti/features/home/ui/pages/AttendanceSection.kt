package com.kanhaji.upasthiti.features.home.ui.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.data.Subject
import com.kanhaji.upasthiti.features.home.ui.HomeScreenModel
import com.kanhaji.upasthiti.features.home.ui.components.SubjectAttendanceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSection(
    screenModel: HomeScreenModel
) {
    val activeTimetable = TimeTableManager.activeTimetableData
    val subjects = Subject.getAllSubjects()
    val sortedSubjects = remember(subjects) {
        subjects.sortedWith(
            compareBy({
                it.displayName.contains("(Lab)", ignoreCase = true) ||
                it.displayName.contains("Lab", ignoreCase = true) ||
                it.displayName.contains("Practical", ignoreCase = true)
            }, { it.displayName })
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Subject Attendance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (sortedSubjects.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No Timetable Loaded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Upload or select a timetable PDF in the Timetable tab to automatically extract your subjects and track attendance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(sortedSubjects, key = { it.subjectId.ifEmpty { it.displayName } }) { subject ->
                var currentMode by remember(subject) {
                    mutableStateOf(com.kanhaji.upasthiti.features.home.data.AttendanceStorage.getSubjectAttendanceMode(subject))
                }

                val attendances = remember(subject) {
                    com.kanhaji.upasthiti.features.home.data.AttendanceStorage.getAttendancesForSubject(subject)
                }

                val (attendedCount, totalCount, percentage) = remember(attendances, currentMode) {
                    if (currentMode == com.kanhaji.upasthiti.features.home.data.AttendanceMode.PER_SLOT) {
                        val total = attendances.size
                        val attended = attendances.count { 
                            it.attendanceStatus == com.kanhaji.upasthiti.features.home.data.AttendanceStatus.PRESENT || 
                            it.attendanceStatus == com.kanhaji.upasthiti.features.home.data.AttendanceStatus.PROXY
                        }
                        val pct = if (total == 0) 0f else (attended.toFloat() / total.toFloat()) * 100f
                        Triple(attended, total, pct)
                    } else {
                        // PER_DAY Mode (Option A: Lenient - Present if any slot on that date is Present/Proxy)
                        val groupedByDate = attendances.groupBy { it.date }
                        val totalDays = groupedByDate.size
                        val attendedDays = groupedByDate.count { (_, slotsOnDate) ->
                            slotsOnDate.any { 
                                it.attendanceStatus == com.kanhaji.upasthiti.features.home.data.AttendanceStatus.PRESENT || 
                                it.attendanceStatus == com.kanhaji.upasthiti.features.home.data.AttendanceStatus.PROXY
                            }
                        }
                        val pct = if (totalDays == 0) 0f else (attendedDays.toFloat() / totalDays.toFloat()) * 100f
                        Triple(attendedDays, totalDays, pct)
                    }
                }

                SubjectAttendanceCard(
                    subject = subject,
                    attendedCount = attendedCount,
                    totalCount = totalCount,
                    percentage = percentage,
                    attendanceMode = currentMode,
                    onToggleMode = {
                        currentMode = com.kanhaji.upasthiti.features.home.data.AttendanceStorage.toggleSubjectAttendanceMode(subject)
                    }
                )
            }
        }
    }
}
