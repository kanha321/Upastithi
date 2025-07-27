package com.kanhaji.upastithi.data.attendance

import kotlinx.serialization.Serializable


@Serializable
enum class AttendanceStatus(val displayName: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    PROXY("Proxy"),
    LEAVE("Leave"),
    HOLIDAY("Holiday"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String?): AttendanceStatus? {
            return entries.find { it.displayName == value }
        }
    }
    override fun toString(): String {
        return displayName
    }
}