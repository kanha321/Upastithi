package com.kanhaji.upasthiti.features.home.data

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
enum class AttendanceStatus(val displayName: String, val color: Color) {
    PRESENT(displayName = "Present", color = Color(0xFF24AB27)),
    ABSENT(displayName = "Absent", color = Color(0xFFAB2424)),
    PROXY(displayName = "Proxy", color = Color(0xFFFFA500)),
    LEAVE(displayName = "Leave", color = Color(0xFF2552C2)),
    HOLIDAY(displayName = "Holiday", color = Color(0xFF8B4513)),
    CANCELLED(displayName = "Cancelled", color = Color(0xFF888888));

    companion object {
        fun fromString(value: String?): AttendanceStatus? {
            return entries.find { it.displayName == value }
        }
    }
    override fun toString(): String {
        return displayName
    }
}
