package com.kanhaji.upastithi.screen.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanhaji.upastithi.data.Subject
import com.kanhaji.upastithi.screen.home.HomeScreenModel

@Composable
fun SubjectCardSingle(
    screenModel: HomeScreenModel,
    subject: Subject,
    refreshKey: Int = 0
) {
    val attendanceInfo = remember(refreshKey) {
        screenModel.getAttendancesForSubject(subject)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$subject",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis

            )
            Text(
                text = "Attendance: ${screenModel.getAttendancesForSubject(subject).second}% (${screenModel.getAttendancesForSubject(subject).first})",
            )
        }
    }
}