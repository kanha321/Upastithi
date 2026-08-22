package com.kanhaji.upasthiti.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


enum class Course {
    MCA1, MCA3
}
object UpasthitiUtils {

    val course: Course
        get() {
            val sem = com.kanhaji.upasthiti.data.TimeTableManager.activeTimetableData?.semester?.lowercase() ?: ""
            return if (sem.contains("3rd") || sem.contains("3rd sem") || sem.contains("mca3") || sem.contains("sem 3")) {
                Course.MCA3
            } else {
                Course.MCA1
            }
        }

    const val BASE_URL = "http://10.204.147.86:8000"
    const val UPDATE_ENDPOINT = "/update.json"

    var updateChecked by mutableStateOf(false)
    var hasCheckedUninstallThisSession = false
    val noClassesMessages = listOf(
        // Motivational
        "🚀 No classes today - perfect time to level up your skills!",
        "💡 Free day ahead! Time to work on that side project.",
        "⭐ Your schedule is clear - opportunity for self-improvement!",
        "📚 No lectures today - dive into that tutorial you bookmarked!",
        "💻 Class-free zone! Perfect day for coding practice.",
        "🌟 Empty schedule = unlimited possibilities!",
        "🎯 No classes today, but endless learning opportunities await!",

        // Technical
        "🔍 404: Classes not found",
        "⚠️ NULL pointer exception - no classes detected",
        "📦 Empty array: classes[] = {}",
//    "Error 204: No class content available",
        "🗄️ SELECT * FROM classes WHERE date = today RETURNED 0 rows",
        "⚡ ClassNotFoundException: No instances found for today",
        "🖨️ printf(\"No classes scheduled for today\\n\");",
        "🖥️ cout << \"No classes today!\" << endl;",
        "☕ System.out.println(\"Enjoy your day, you're free today!\");",
        "⚙️ if (classes.isEmpty()) { enjoyFreeTime(); }",
        "🐍 def no_classes_today(): return True",
        "🌿 git commit -m 'No classes today!'",
        "🛠️ npm install free-time --save",
//    "HTTP 204: No class content",

        // Sarcastic
        "🥳 Congratulations! You've successfully avoided all responsibilities today.",
        "📰 Breaking news: Student discovers legendary 'free day' in schedule",
        "🛡️ Your attendance streak is safe - there's literally nothing to attend!",
//    "Plot twist: The universe gave you a day off",
//    "Achievement unlocked: Found the mythical 'no classes' day",
        "🎁 Surprise! Your calendar app isn't broken, you're just lucky.",
        "🛋️ Error: Could not locate any reason to leave your room today",

        // Original
        "🌿 It's a quiet day! No classes available for attendance."
    )


    var appVersionCode: Long = -1
    var appVersionName: String? = null
}
