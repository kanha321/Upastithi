package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kanhaji.upasthiti.features.home.data.Subject

@Composable
fun SubjectAttendanceCard(
    subject: Subject,
    attendedCount: Int,
    totalCount: Int,
    percentage: Float
) {
    val isLab = subject.displayName.contains("(Lab)", ignoreCase = true) ||
                subject.displayName.contains("Lab", ignoreCase = true) ||
                subject.displayName.contains("Practical", ignoreCase = true)

    val isNewSubject = totalCount == 0

    val attendanceColor = when {
        isNewSubject -> MaterialTheme.colorScheme.onSurfaceVariant
        percentage >= 85f -> MaterialTheme.colorScheme.primary
        percentage >= 75f -> MaterialTheme.colorScheme.tertiary
        percentage < 50f -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
    }

    val containerColor = when {
        isLab -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
        isNewSubject -> MaterialTheme.colorScheme.surfaceContainerLow
        percentage < 50f -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        else -> attendanceColor.copy(alpha = 0.08f)
    }

    val borderColor = when {
        isLab -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        isNewSubject -> MaterialTheme.colorScheme.outlineVariant
        percentage < 50f -> MaterialTheme.colorScheme.error
        else -> attendanceColor.copy(alpha = 0.35f)
    }

    val statusText = when {
        isNewSubject -> "No Data"
        percentage >= 85f -> "Excellent"
        percentage >= 75f -> "Good"
        percentage < 50f -> "Critical (<50%)"
        else -> "Low"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(width = if (!isNewSubject && percentage < 50f) 1.5.dp else 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Subject Code Pills (Left) & Large Percentage Display (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLab) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = subject.subjectId.ifEmpty { "GENERAL" },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLab) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (isLab) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Practical",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Lecture",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Top Right: Percentage Display (or 0/0 for new subjects)
                Text(
                    text = if (isNewSubject) "0/0" else "${percentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = attendanceColor
                )
            }

            // Subject Title & Teacher Info
            val cleanTitle = remember(subject.displayName) {
                subject.displayName
                    .replace(Regex("(?i)^\\(lab\\)\\s*"), "")
                    .replace(Regex("(?i)\\s*\\(lab\\)$"), "")
                    .trim()
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = cleanTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (subject.teacher.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Teacher",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = subject.teacher,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Progress Bar & Clean Breakdown Row (Status Badge on Right)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { if (isNewSubject) 0f else (percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (isNewSubject) MaterialTheme.colorScheme.outlineVariant else attendanceColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$attendedCount/$totalCount attended",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    // Right Side: Clean Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNewSubject) MaterialTheme.colorScheme.surfaceVariant else attendanceColor
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNewSubject) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
