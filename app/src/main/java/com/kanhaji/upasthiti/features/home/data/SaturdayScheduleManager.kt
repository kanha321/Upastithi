package com.kanhaji.upasthiti.features.home.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kanhaji.basics.datastore.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SaturdayMode {
    AUTO,
    MANUAL
}

object SaturdayScheduleManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }

    var isEnabled by mutableStateOf(false)
        private set

    var mode by mutableStateOf(SaturdayMode.AUTO)
        private set

    var startDate by mutableStateOf(LocalDate(2026, 8, 22))
        private set

    var manualDay by mutableStateOf(DayOfWeek.MONDAY)
        private set

    var isCardDismissed by mutableStateOf(false)
        private set

    val dateOverrides = mutableStateMapOf<String, String>()

    suspend fun loadSettings() {
        try {
            isEnabled = PrefsManager.getBoolean("saturday_schedule_enabled") ?: false
            val modeStr = PrefsManager.getString("saturday_schedule_mode")
            mode = if (modeStr == "MANUAL") SaturdayMode.MANUAL else SaturdayMode.AUTO

            val startDateStr = PrefsManager.getString("saturday_schedule_start_date")
            startDate = if (!startDateStr.isNullOrBlank()) {
                try { LocalDate.parse(startDateStr) } catch (e: Exception) { LocalDate(2026, 8, 22) }
            } else {
                LocalDate(2026, 8, 22)
            }

            val manualDayStr = PrefsManager.getString("saturday_schedule_manual_day")
            manualDay = if (!manualDayStr.isNullOrBlank()) {
                try { DayOfWeek.valueOf(manualDayStr) } catch (e: Exception) { DayOfWeek.MONDAY }
            } else {
                DayOfWeek.MONDAY
            }

            isCardDismissed = PrefsManager.getBoolean("saturday_card_dismissed") ?: false

            val overridesJson = PrefsManager.getString("saturday_date_overrides")
            dateOverrides.clear()
            if (!overridesJson.isNullOrBlank()) {
                try {
                    val map = json.decodeFromString<Map<String, String>>(overridesJson)
                    dateOverrides.putAll(map)
                } catch (e: Exception) {
                    // Ignore parse error
                }
            }
        } catch (e: Exception) {
            // Ignore load error
        }
    }

    fun updateEnabled(enabled: Boolean) {
        isEnabled = enabled
        scope.launch {
            PrefsManager.saveBoolean("saturday_schedule_enabled", enabled)
        }
    }

    fun updateScheduleMode(newMode: SaturdayMode) {
        mode = newMode
        scope.launch {
            PrefsManager.saveString("saturday_schedule_mode", newMode.name)
        }
    }

    fun updateStartDate(newDate: LocalDate) {
        startDate = newDate
        scope.launch {
            PrefsManager.saveString("saturday_schedule_start_date", newDate.toString())
        }
    }

    fun updateManualDay(day: DayOfWeek) {
        manualDay = day
        scope.launch {
            PrefsManager.saveString("saturday_schedule_manual_day", day.name)
        }
    }

    fun updateCardDismissed(dismissed: Boolean) {
        isCardDismissed = dismissed
        scope.launch {
            PrefsManager.saveBoolean("saturday_card_dismissed", dismissed)
        }
    }

    fun setDateOverride(date: LocalDate, dayOfWeek: DayOfWeek?) {
        val key = date.toString()
        if (dayOfWeek == null) {
            dateOverrides.remove(key)
        } else {
            dateOverrides[key] = dayOfWeek.name
        }
        val copy = dateOverrides.toMap()
        scope.launch {
            PrefsManager.saveString("saturday_date_overrides", json.encodeToString(copy))
        }
    }

    /**
     * Resolves which weekday schedule a given date should follow.
     * Returns null if date is not Saturday or if Saturday schedule is disabled.
     */
    fun resolveDayOfWeek(date: LocalDate): DayOfWeek? {
        if (date.dayOfWeek != DayOfWeek.SATURDAY || !isEnabled) return null

        // 1. Check custom date override
        val overrideStr = dateOverrides[date.toString()]
        if (!overrideStr.isNullOrBlank()) {
            return try { DayOfWeek.valueOf(overrideStr) } catch (e: Exception) { null }
        }

        // 2. Check mode
        return when (mode) {
            SaturdayMode.MANUAL -> manualDay
            SaturdayMode.AUTO -> {
                if (date < startDate) return null
                val daysDiff = startDate.daysUntil(date)
                if (daysDiff < 0) return null
                val weeksDiff = daysDiff / 7
                val weekdayCycle = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                )
                weekdayCycle[weeksDiff % weekdayCycle.size]
            }
        }
    }

    fun getUpcomingSaturdays(count: Int = 8): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var epochDays = startDate.toEpochDays()
        // Align epochDays to Saturday
        while (LocalDate.fromEpochDays(epochDays).dayOfWeek != DayOfWeek.SATURDAY) {
            epochDays++
        }
        for (i in 0 until count) {
            result.add(LocalDate.fromEpochDays(epochDays))
            epochDays += 7
        }
        return result
    }
}
