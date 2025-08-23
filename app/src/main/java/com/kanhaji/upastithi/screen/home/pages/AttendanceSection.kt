package com.kanhaji.upastithi.screen.home.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.screen.home.HomeScreenModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceSection(
    screenModel: HomeScreenModel
) {
    val refreshKey = remember { mutableIntStateOf(0) }
    val subjects = Subject.getAllSubjects()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Subject Attendance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(subjects) { subject ->
            SubjectAttendanceCard(
                subject = subject,
                screenModel = screenModel,
                refreshKey = refreshKey.intValue
            )
        }
    }
}

@Composable
private fun SubjectAttendanceCard(
    subject: Subject,
    screenModel: HomeScreenModel,
    refreshKey: Int
) {
    val attendanceInfo = remember(refreshKey) {
        screenModel.getAttendancesForSubject(subject)
    }

    val (attendanceText, percentage) = attendanceInfo
    val attendanceColor = when {
        percentage >= 85 -> MaterialTheme.colorScheme.primary
        percentage >= 75 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            color = MaterialTheme.colorScheme.primary,
            width = 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subject.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subject.subjectId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = subject.teacher,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Attendance Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = attendanceColor
                )

                Text(
                    text = attendanceText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Attendance Status Indicator
                Surface(
                    modifier = Modifier
                        .padding(top = 4.dp),
                    shape = MaterialTheme.shapes.small,
                    color = attendanceColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = when {
                            percentage >= 85 -> "Excellent"
                            percentage >= 75 -> "Good"
                            else -> "Low"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = attendanceColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            color = attendanceColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}