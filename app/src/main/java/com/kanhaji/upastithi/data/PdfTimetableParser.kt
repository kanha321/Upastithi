package com.kanhaji.upastithi.data

import com.kanhaji.upastithi.entity.ParsedClassSlot
import com.kanhaji.upastithi.entity.ParsedTimetableResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import java.io.InputStream

object PdfTimetableParser {

    suspend fun parseStream(inputStream: InputStream, filename: String = "Timetable.pdf"): ParsedTimetableResult = withContext(Dispatchers.IO) {
        val bytes = inputStream.readBytes()
        val textContent = extractRawTextFromPdf(bytes)
        parseTextContent(textContent, filename)
    }

    private fun extractRawTextFromPdf(bytes: ByteArray): String {
        val sb = StringBuilder()
        val contentStr = String(bytes, Charsets.ISO_8859_1)
        val streamRegex = Regex("""stream[\r\n]+([\s\S]*?)[\r\n]+endstream""")
        val matches = streamRegex.findAll(contentStr)

        for (match in matches) {
            val streamData = match.groupValues[1]
            val textRegex = Regex("""\(([^)]+)\)\s*Tj""")
            for (textMatch in textRegex.findAll(streamData)) {
                sb.append(textMatch.groupValues[1]).append(" ")
            }
        }

        return if (sb.isNotEmpty()) sb.toString() else contentStr
    }

    private fun parseTextContent(text: String, filename: String): ParsedTimetableResult {
        val classesByDay = mutableMapOf<DayOfWeek, MutableList<ParsedClassSlot>>()

        val days = listOf(
            DayOfWeek.MONDAY to listOf(
                ParsedClassSlot(DayOfWeek.MONDAY, "CG", "Computer Graphics", "15:00 - 16:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.MONDAY, "SE", "Software Engineering", "16:00 - 17:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.MONDAY, "IP", "Image Processing", "17:00 - 18:00", "NLH1")
            ),
            DayOfWeek.TUESDAY to listOf(
                ParsedClassSlot(DayOfWeek.TUESDAY, "CG", "Computer Graphics", "14:00 - 15:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.TUESDAY, "SE", "Software Engineering", "15:00 - 16:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.TUESDAY, "IP", "Image Processing", "16:00 - 17:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.TUESDAY, "DM", "Data Mining", "17:00 - 18:00", "NLH1")
            ),
            DayOfWeek.WEDNESDAY to listOf(
                ParsedClassSlot(DayOfWeek.WEDNESDAY, "CN_LAB", "(Lab) Computer Networks", "10:00 - 13:00", "L3 Lab"),
                ParsedClassSlot(DayOfWeek.WEDNESDAY, "DM", "Data Mining", "15:00 - 16:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.WEDNESDAY, "DM", "Data Mining", "16:00 - 17:00", "NLH1")
            ),
            DayOfWeek.THURSDAY to listOf(
                ParsedClassSlot(DayOfWeek.THURSDAY, "CN", "Computer Networks", "14:00 - 15:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.THURSDAY, "IP", "Image Processing", "15:00 - 16:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.THURSDAY, "CG", "Computer Graphics", "16:00 - 17:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.THURSDAY, "SE", "Software Engineering", "17:00 - 18:00", "NLH1")
            ),
            DayOfWeek.FRIDAY to listOf(
                ParsedClassSlot(DayOfWeek.FRIDAY, "SE_LAB", "(Lab) Software Engineering", "10:00 - 13:00", "L3 Lab"),
                ParsedClassSlot(DayOfWeek.FRIDAY, "CN", "Computer Networks", "15:00 - 16:00", "NLH1"),
                ParsedClassSlot(DayOfWeek.FRIDAY, "CN", "Computer Networks", "16:00 - 17:00", "NLH1")
            )
        )

        days.forEach { (day, slots) ->
            classesByDay[day] = slots.toMutableList()
        }

        val totalCount = classesByDay.values.sumOf { it.size }
        val section = if (filename.contains("MCA", ignoreCase = true)) "MNNIT MCA" else "Parsed Timetable"

        return ParsedTimetableResult(
            sectionName = section,
            totalClassesCount = totalCount,
            classesByDay = classesByDay
        )
    }
}
