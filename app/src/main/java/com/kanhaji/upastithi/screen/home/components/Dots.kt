// File: src/main/java/com/kanhaji/upastithi/screen/home/components/Dots.kt
package com.kanhaji.upastithi.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.entity.AttendanceEntity
import com.kanhaji.upastithi.util.getClasses
import kotlinx.datetime.LocalDate

private data class DotSpec(
    val color: Color,
    val outlined: Boolean,
    val startMinutes: Int
)

@Composable
fun MultiDotIndicator(
    date: LocalDate,
    allAttendances: List<AttendanceEntity>
) {
    val scheduledClasses = date.dayOfWeek.getClasses()

    fun parseStartMinutes(timeRange: String): Int {
        val start = timeRange.substringBefore(" - ")
        val (h, m) = start.split(":").let {
            (it.getOrNull(0)?.toIntOrNull() ?: 0) to (it.getOrNull(1)?.toIntOrNull() ?: 0)
        }
        return h * 60 + m
    }

    val dots = scheduledClasses
        .sortedBy { parseStartMinutes(it.time) }
        .map { classEntity ->
            val attendance = allAttendances.firstOrNull {
                it.time == classEntity.time && it.attendanceStatus != null
            }
            if (attendance != null) {
                DotSpec(
                    color = attendance.attendanceStatus!!.color,
                    outlined = false,
                    startMinutes = parseStartMinutes(attendance.time)
                )
            } else {
                DotSpec(
                    color = MaterialTheme.colorScheme.primary,
                    outlined = true,
                    startMinutes = parseStartMinutes(classEntity.time)
                )
            }
        }

    if (dots.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            dots.forEach { spec ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .then(
                            if (spec.outlined) {
                                Modifier
                                    .border(1.dp, spec.color, CircleShape)
                            } else {
                                Modifier
                                    .background(spec.color)
                            }
                        )
                )
            }
        }
    }
}