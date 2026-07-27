package com.kanhaji.upasthiti.screen.edit

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent

data class EditClassScreen(
    val event: ScheduleEvent? = null,
    val initialDay: String = "Monday"
) : Screen {

    @Composable
    override fun Content() {
        EditClassComponent(
            event = event,
            initialDay = initialDay
        )
    }
}
