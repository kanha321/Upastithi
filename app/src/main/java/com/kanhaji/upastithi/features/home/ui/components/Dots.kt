package com.kanhaji.upastithi.features.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.features.home.domain.model.AttendanceEntity
import com.kanhaji.upastithi.util.getClasses
import kotlinx.datetime.LocalDate

/** Fixed dot diameter — identical across every calendar cell. */
private val DOT_SIZE = 6.5.dp
private val DOT_SPACING = 2.dp
private val ROW_SPACING = 1.5.dp

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
        val parts = start.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
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

    if (dots.isEmpty()) return

    if (dots.size <= 4) {
        // Single row for 1–4 dots
        DotRow(dots)
    } else {
        // Dual row for 5–8 dots
        // Split: top row gets ceil(n/2), bottom row gets the rest
        val topCount = (dots.size + 1) / 2 // ceil division
        val topRow = dots.take(topCount)
        val bottomRow = dots.drop(topCount)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ROW_SPACING)
        ) {
            DotRow(topRow)
            DotRow(bottomRow)
        }
    }
}

@Composable
private fun DotRow(specs: List<DotSpec>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(DOT_SPACING, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        specs.forEach { spec ->
            Box(
                modifier = Modifier
                    .size(DOT_SIZE)
                    .clip(CircleShape)
                    .then(
                        if (spec.outlined) {
                            Modifier.border(1.dp, spec.color, CircleShape)
                        } else {
                            Modifier.background(spec.color)
                        }
                    )
            )
        }
    }
}
