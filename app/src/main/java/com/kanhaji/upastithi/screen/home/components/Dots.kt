package com.kanhaji.upastithi.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.entity.AttendanceEntity
import com.kanhaji.upastithi.screen.home.HomeScreenModel
import kotlinx.datetime.LocalDate

@Composable
fun MultiDotIndicator(date: LocalDate, allAttendances: List<AttendanceEntity>, screenModel: HomeScreenModel) {
    val dotColors = screenModel.getAttendanceDotsForDate(date, allAttendances)

    if (dotColors.isNotEmpty()) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            dotColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
