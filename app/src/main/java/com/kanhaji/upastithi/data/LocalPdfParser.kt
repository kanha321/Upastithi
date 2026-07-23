package com.kanhaji.upastithi.data

import android.util.Log
import com.kanhaji.upastithi.features.home.domain.model.CourseInfo
import com.kanhaji.upastithi.features.home.domain.model.DetectedTimetable
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent
import com.kanhaji.upastithi.features.home.domain.model.TimetableData
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.regex.Pattern

data class CharPos(
    val char: Char,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class TextBlock(
    val text: String,
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float
) {
    val centerX: Float get() = (minX + maxX) / 2
    val centerY: Float get() = (minY + maxY) / 2
}

class TimetableStripper : PDFTextStripper() {
    val charList = mutableListOf<CharPos>()

    init {
        sortByPosition = true
    }

    @Throws(IOException::class)
    override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
        if (textPositions == null) return
        for (tp in textPositions) {
            val unicode = tp.unicode
            if (unicode != null && unicode.isNotEmpty()) {
                charList.add(
                    CharPos(
                        char = unicode[0],
                        x = tp.xDirAdj,
                        y = tp.yDirAdj,
                        width = tp.widthDirAdj,
                        height = tp.heightDir
                    )
                )
            }
        }
    }
}

object LocalPdfParser {

    private const val TAG = "LocalPdfParser"

    private val DEFAULT_SLOTS = listOf(
        "08:00-09:00",
        "09:00-10:00",
        "10:00-11:00",
        "11:00-12:00",
        "12:00-13:00",
        "13:00-14:00",
        "14:00-15:00",
        "15:00-16:00",
        "16:00-17:00",
        "17:00-18:00"
    )

    private val DEFAULT_SLOT_CENTERS = listOf(
        108.2f, // 08:00 - 09:00
        172.0f, // 09:00 - 10:00
        249.7f, // 10:00 - 11:00
        336.0f, // 11:00 - 12:00
        406.0f, // 12:00 - 13:00 (Lunch)
        480.0f, // 13:00 - 14:00
        550.0f, // 14:00 - 15:00
        625.0f, // 15:00 - 16:00
        713.0f, // 16:00 - 17:00
        790.0f  // 17:00 - 18:00
    )

    fun matchInitialsToName(initials: String, fullName: String): Boolean {
        val cleanName = fullName.replace("^(Prof\\.|Dr\\.|Mr\\.|Mrs\\.)".toRegex(), "")
            .replace(".", " ")
            .trim()
        val words = cleanName.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (words.isEmpty()) return false
        
        val abbr = initials.uppercase()
        
        // Case 1: Exact first-letter sequence match (e.g. SSS -> S(hreeya) S(wagatika) S(ahoo))
        val firstLetters = words.map { it.first().uppercaseChar() }.joinToString("")
        if (abbr == firstLetters) return true
        
        // Case 2: Initials prefix + second word start (e.g. KOG -> KO(mal) G(upta))
        if (words.size >= 2) {
            val firstWord = words[0].uppercase()
            val secondWord = words[1].uppercase()
            if (abbr.first() == firstWord.first() && abbr.last() == secondWord.first()) {
                return true
            }
        }
        
        // Case 3: Substring prefix overlap for single-word names (e.g. RAN -> RANvijay)
        if (words.size == 1) {
            val firstWord = words[0].uppercase()
            if (firstWord.startsWith(abbr)) return true
        }
        
        return false
    }

    private fun cleanDay(dayText: String): String {
        val clean = dayText.replace("\n", "").trim()
        if (clean.equals("wednesda", ignoreCase = true)) {
            return "Wednesday"
        }
        return clean.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun splitTimeSlot(slotText: String): Pair<String, String> {
        val parts = slotText.split("[-–—]".toRegex()).filter { it.isNotEmpty() }
        if (parts.size >= 2) {
            return Pair(parts[0].trim(), parts[1].trim())
        }
        return Pair(slotText, slotText)
    }

    private fun cleanCourseName(rawName: String, initials: List<String>): String {
        var cleaned = rawName
        for (init in initials) {
            cleaned = cleaned.replace("\\b$init\\b".toRegex(), "")
        }
        cleaned = cleaned.replace("\\([^)]+\\)".toRegex(), "")
        cleaned = cleaned.replace("\\s+".toRegex(), " ")
        
        while (true) {
            val prev = cleaned
            cleaned = cleaned.trim()
                .removePrefix(",").removePrefix(".").removePrefix("-")
                .removeSuffix(",").removeSuffix(".").removeSuffix("-")
                .trim()
            if (prev == cleaned) break
        }
        return cleaned
    }

    private fun findCourseCodeInCell(cellText: String, courses: List<CourseInfo>): String? {
        for (c in courses) {
            val pattern = Pattern.compile("\\b${c.code}\\b", Pattern.CASE_INSENSITIVE)
            if (pattern.matcher(cellText).find()) {
                return c.code
            }
        }
        val m = Pattern.compile("\\b(?:[A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\b", Pattern.CASE_INSENSITIVE).matcher(cellText)
        while (m.find()) {
            val candidate = m.group(0)!!
            val isRoom = listOf("GS1","GS2","GS3","GS4","GS5","GS6","GS7","GS8","NLH1","NLH2","NLH3","NLH4").any { it.equals(candidate, ignoreCase = true) }
            if (!isRoom) {
                return candidate
            }
        }
        return null
    }

    fun isLikelyTimetable(fileBytes: ByteArray): Boolean {
        try {
            PDDocument.load(ByteArrayInputStream(fileBytes)).use { document ->
                if (document.numberOfPages == 0) return false
                val stripper = TimetableStripper()
                stripper.startPage = 1
                stripper.endPage = 1
                stripper.getText(document)
                val text = stripper.charList.map { it.char }.joinToString("").lowercase()
                
                val hasDays = listOf("monday", "tuesday", "wednesday", "thursday", "friday").any { text.contains(it) }
                val hasKeywords = listOf("time table", "timetable", "schedule", "semester").any { text.contains(it) }
                val hasTimeSlots = Pattern.compile("\\d{2}:\\d{2}").matcher(text).find()
                
                return hasDays && (hasKeywords || hasTimeSlots)
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun detectTimetables(fileBytes: ByteArray): List<DetectedTimetable> {
        Log.d(TAG, "detectTimetables: Loading document bytes, size = ${fileBytes.size}")
        val timetables = mutableListOf<DetectedTimetable>()
        try {
            PDDocument.load(ByteArrayInputStream(fileBytes)).use { document ->
                Log.d(TAG, "detectTimetables: Document loaded. Total pages = ${document.numberOfPages}")
                for (pageIdx in 0 until document.numberOfPages) {
                    val stripper = TimetableStripper()
                    stripper.startPage = pageIdx + 1
                    stripper.endPage = pageIdx + 1
                    stripper.getText(document)
                    
                    val textContent = stripper.charList.map { it.char }.joinToString("")
                    var semesterName = "Unknown Semester"
                    val semPattern = Pattern.compile("([A-Za-z0-9.\\-\\s]+?\\b\\d+(?:st|nd|rd|th)?\\s+Semester)", Pattern.CASE_INSENSITIVE)
                    val matcher = semPattern.matcher(textContent)
                    if (matcher.find()) {
                        semesterName = matcher.group(1)?.trim() ?: "Semester ${pageIdx + 1}"
                    } else {
                        semesterName = "Semester ${pageIdx + 1}"
                    }
                    Log.d(TAG, "detectTimetables: Page $pageIdx detected as: $semesterName")
                    timetables.add(DetectedTimetable(pageIdx, semesterName))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "detectTimetables failed: ${e.message}", e)
        }
        return timetables
    }

    fun parseTimetablePage(fileBytes: ByteArray, pageIdx: Int): TimetableData {
        Log.d(TAG, "parseTimetablePage: Starting offline parse for page index: $pageIdx")
        try {
            PDDocument.load(ByteArrayInputStream(fileBytes)).use { document ->
                val stripper = TimetableStripper()
                stripper.startPage = pageIdx + 1
                stripper.endPage = pageIdx + 1
                stripper.getText(document)

                Log.d(TAG, "parseTimetablePage: Stripper extracted ${stripper.charList.size} characters")

                val sortedChars = stripper.charList.sortedWith(compareBy({ it.y }, { it.x }))
                val lines = mutableListOf<MutableList<CharPos>>()
                for (char in sortedChars) {
                    var added = false
                    for (line in lines) {
                        if (Math.abs(char.y - line[0].y) < 4.5f) {
                            line.add(char)
                            added = true
                            break
                        }
                    }
                    if (!added) {
                        lines.add(mutableListOf(char))
                    }
                }
                Log.d(TAG, "parseTimetablePage: Grouped characters into ${lines.size} horizontal lines")

                val blocks = mutableListOf<TextBlock>()
                for (line in lines) {
                    val sortedLine = line.sortedBy { it.x }
                    var currentBlock = StringBuilder()
                    var minX = 0f
                    var maxX = 0f
                    var minY = 0f
                    var maxY = 0f
                    
                    for (i in sortedLine.indices) {
                        val c = sortedLine[i]
                        if (currentBlock.isEmpty()) {
                            currentBlock.append(c.char)
                            minX = c.x
                            maxX = c.x + c.width
                            minY = c.y
                            maxY = c.y + c.height
                        } else {
                            val prev = sortedLine[i - 1]
                            val gap = c.x - (prev.x + prev.width)
                            if (gap < 8.0f) {
                                currentBlock.append(c.char)
                                maxX = c.x + c.width
                                minY = Math.min(minY, c.y)
                                maxY = Math.max(maxY, c.y + c.height)
                            } else {
                                blocks.add(TextBlock(currentBlock.toString(), minX, maxX, minY, maxY))
                                currentBlock = StringBuilder().append(c.char)
                                minX = c.x
                                maxX = c.x + c.width
                                minY = c.y
                                maxY = c.y + c.height
                            }
                        }
                    }
                    if (currentBlock.isNotEmpty()) {
                        blocks.add(TextBlock(currentBlock.toString(), minX, maxX, minY, maxY))
                    }
                }
                Log.d(TAG, "parseTimetablePage: Processed ${blocks.size} word blocks on page")

                val fullTextContent = stripper.charList.map { it.char }.joinToString("")
                var semesterName = "Unknown Semester"
                val semPattern = Pattern.compile("([A-Za-z0-9.\\-\\s]+?\\b\\d+(?:st|nd|rd|th)?\\s+Semester)", Pattern.CASE_INSENSITIVE)
                val semMatcher = semPattern.matcher(fullTextContent)
                if (semMatcher.find()) {
                    semesterName = semMatcher.group(1)?.trim() ?: "Semester ${pageIdx + 1}"
                } else {
                    val topBlocks = blocks.filter { it.centerY < 110f }
                    val possibleSemBlock = topBlocks.find { b ->
                        val txt = b.text.trim()
                        !txt.contains("Motilal", ignoreCase = true) &&
                        !txt.contains("National", ignoreCase = true) &&
                        !txt.contains("Institute", ignoreCase = true) &&
                        !txt.contains("Technology", ignoreCase = true) &&
                        !txt.contains("Allahabad", ignoreCase = true) &&
                        !txt.contains("Time Table", ignoreCase = true) &&
                        !txt.contains("Timetable", ignoreCase = true) &&
                        !txt.contains("08:00", ignoreCase = true) &&
                        !txt.contains("Hrs.", ignoreCase = true)
                    }
                    semesterName = possibleSemBlock?.text?.trim() ?: "Semester ${pageIdx + 1}"
                }
                Log.d(TAG, "parseTimetablePage: Extracted semester title: $semesterName")

                val dayYMap = mutableMapOf<String, Float>()
                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Wednesda")
                for (b in blocks) {
                    val clean = b.text.trim()
                    val matchedDay = days.find { it.equals(clean, ignoreCase = true) }
                    if (matchedDay != null) {
                        dayYMap[cleanDay(matchedDay)] = b.centerY
                    }
                }
                Log.d(TAG, "parseTimetablePage: Extracted day Y-coordinates: $dayYMap")

                val maxDayY = (dayYMap.values.maxOrNull() ?: 250f) + 15f

                val slotTimePattern = Pattern.compile("\\d{2}:\\d{2}\\s*[-–—]\\s*\\d{2}:\\d{2}")
                val matchingHeaderBlocks = blocks.filter { slotTimePattern.matcher(it.text).find() }
                
                val headerRows = mutableListOf<MutableList<TextBlock>>()
                for (b in matchingHeaderBlocks) {
                    var added = false
                    for (row in headerRows) {
                        if (Math.abs(b.centerY - row[0].centerY) < 6f) {
                            row.add(b)
                            added = true
                            break
                        }
                    }
                    if (!added) {
                        headerRows.add(mutableListOf(b))
                    }
                }
                
                val slotHeaderRow = headerRows.maxByOrNull { it.size }?.sortedBy { it.centerX } ?: emptyList()
                val slots = mutableListOf<String>()
                val slotCenters = mutableListOf<Float>()
                
                if (slotHeaderRow.size == 10) {
                    for (b in slotHeaderRow) {
                        val m = slotTimePattern.matcher(b.text)
                        val timeStr = if (m.find()) m.group(0)!!.replace("\\s+".toRegex(), "") else b.text
                        slots.add(timeStr)
                        slotCenters.add(b.centerX)
                    }
                } else {
                    slots.addAll(DEFAULT_SLOTS)
                    slotCenters.addAll(DEFAULT_SLOT_CENTERS)
                }

                fun getSlotIndex(centerX: Float): Int {
                    var minDiff = Float.MAX_VALUE
                    var slotIdx = -1
                    for (i in slotCenters.indices) {
                        val diff = Math.abs(centerX - slotCenters[i])
                        if (diff < minDiff) {
                            minDiff = diff
                            slotIdx = i
                        }
                    }
                    return slotIdx
                }

                val leftBlocks = blocks.filter { it.centerY >= maxDayY && it.centerX < 390f }
                    .sortedWith(compareBy({ it.centerY }, { it.minX }))
                val rightBlocks = blocks.filter { it.centerY >= maxDayY && it.centerX >= 390f }
                    .sortedWith(compareBy({ it.centerY }, { it.minX }))
                
                val leftText = leftBlocks.joinToString(" ") { it.text }
                val rightText = rightBlocks.joinToString(" ") { it.text }

                val nameList = mutableListOf<String>()
                val namePattern = Pattern.compile("(Prof\\.|Dr\\.|Mr\\.|Mrs\\.)\\s+[A-Z][a-zA-Z.]+(?:\\s+[A-Z][a-zA-Z.]+)*")
                for (text in listOf(leftText, rightText)) {
                    val nameMatcher = namePattern.matcher(text)
                    while (nameMatcher.find()) {
                        val parsedName = nameMatcher.group(0)!!.trim().replace("\\s+".toRegex(), " ")
                        nameList.add(parsedName)
                    }
                }

                val initialsList = mutableListOf<String>()
                val initialsPattern = Pattern.compile("([A-Z]{2,5})\\s*:")
                for (text in listOf(leftText, rightText)) {
                    val initialsMatcher = initialsPattern.matcher(text)
                    while (initialsMatcher.find()) {
                        initialsList.add(initialsMatcher.group(1)!!.trim())
                    }
                }

                val facultyMap = mutableMapOf<String, String>()
                for (init in initialsList) {
                    val matchName = nameList.find { matchInitialsToName(init, it) }
                    if (matchName != null) {
                        facultyMap[init] = matchName
                    } else {
                        var fallbackResolved = false
                        for (text in listOf(leftText, rightText)) {
                            val fallbackPattern = Pattern.compile("\\b$init\\s*:\\s*(Prof\\.|Dr\\.|Mr\\.|Mrs\\.)?\\s*([^,:]+)")
                            val fallbackMatcher = fallbackPattern.matcher(text)
                            if (fallbackMatcher.find()) {
                                val title = fallbackMatcher.group(1)?.trim() ?: ""
                                val restName = fallbackMatcher.group(2)?.trim() ?: ""
                                val cleanName = (if (title.isNotEmpty()) "$title " else "") + restName.replace("\n", " ").trim()
                                val finalName = cleanName.replace("\\s+".toRegex(), " ").trim()
                                facultyMap[init] = finalName
                                fallbackResolved = true
                                break
                            }
                        }
                        if (!fallbackResolved) {
                            facultyMap[init] = init
                        }
                    }
                }

                val courses = mutableListOf<CourseInfo>()
                val coursePattern = Pattern.compile("([A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\s*=\\s*(.*?)(?=\\s*(?:[A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\s*\\(|\\s*(?:[A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\s*=|\\s*[A-Z]{2,5}\\s*:|$)", Pattern.CASE_INSENSITIVE)
                
                for (text in listOf(leftText, rightText)) {
                    val courseMatcher = coursePattern.matcher(text)
                    while (courseMatcher.find()) {
                        val code = courseMatcher.group(1)!!.trim()
                        val rest = courseMatcher.group(2)!!.trim()
                        
                        val facList = mutableListOf<String>()
                        for (init in initialsList) {
                            if (Pattern.compile("\\b$init\\b").matcher(rest).find()) {
                                facList.add(init)
                            }
                        }
                        
                        val detailsPattern = Pattern.compile("\\(([^)]+)\\)")
                        val detailsMatcher = detailsPattern.matcher(rest)
                        var details = ""
                        if (detailsMatcher.find()) {
                            details = detailsMatcher.group(1)!!.trim()
                        }

                        val name = cleanCourseName(rest, initialsList)
                        
                        if (code.isNotEmpty() && !courses.any { it.code.equals(code, ignoreCase = true) }) {
                            courses.add(CourseInfo(code, name, details, facList))
                        }
                    }
                }

                val backupCoursePattern = Pattern.compile("([A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\s+\\(([^)]+)\\)\\s*(.*?)(?=\\s*(?:[A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\s*\\(|\\s*(?:[A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\s*=|\\s*[A-Z]{2,5}\\s*:|$)", Pattern.CASE_INSENSITIVE)
                for (text in listOf(leftText, rightText)) {
                    val backupMatcher = backupCoursePattern.matcher(text)
                    while (backupMatcher.find()) {
                        val code = backupMatcher.group(1)!!.trim()
                        val details = backupMatcher.group(2)!!.trim()
                        val rest = backupMatcher.group(3)!!.trim()
                        
                        if (courses.any { it.code.equals(code, ignoreCase = true) }) continue
                        if (days.any { it.equals(code, ignoreCase = true) } || initialsList.any { it.equals(code, ignoreCase = true) }) continue
                        if (code == "LUNCH" || code.startsWith("Time") || code.startsWith("Motilal")) continue
                        
                        val facList = mutableListOf<String>()
                        for (init in initialsList) {
                            if (Pattern.compile("\\b$init\\b").matcher(rest).find()) {
                                facList.add(init)
                            }
                        }
                        
                        val name = cleanCourseName(rest, initialsList)
                        
                        if (!courses.any { it.code.equals(code, ignoreCase = true) }) {
                            courses.add(CourseInfo(code, name, details, facList))
                        }
                    }
                }

                val schedule = mutableListOf<ScheduleEvent>()
                val cellGroupMap = mutableMapOf<String, MutableList<TextBlock>>()

                for (b in blocks) {
                    if (b.centerY >= maxDayY) continue
                    val text = b.text.trim()
                    if (text == "LUNCH" || text == "Monday" || text == "Tuesday" || text == "Wednesday" || text == "Thursday" || text == "Friday" || text == "Wednesda" || text.startsWith("Time Table") || text.startsWith("Motilal") || text.contains("Hrs.") || text.contains("08:00") || text.contains("Semester")) {
                        continue
                    }

                    var closestDay = ""
                    val sortedDays = dayYMap.toList().sortedBy { it.second }
                    for (i in sortedDays.indices) {
                        val (day, yVal) = sortedDays[i]
                        val nextYVal = if (i + 1 < sortedDays.size) sortedDays[i + 1].second else Float.MAX_VALUE
                        if (b.centerY >= (yVal - 4.0f) && b.centerY < (nextYVal - 4.0f)) {
                            closestDay = day
                            break
                        }
                    }
                    if (closestDay.isEmpty()) continue

                    val colIdx = getSlotIndex(b.centerX)
                    if (colIdx == -1) continue

                    val key = "$closestDay:$colIdx"
                    cellGroupMap.getOrPut(key) { mutableListOf() }.add(b)
                }

                for ((key, cellBlocks) in cellGroupMap) {
                    val parts = key.split(":")
                    val dayName = parts[0]
                    val slotIdx = parts[1].toInt()

                    val sortedCellBlocks = cellBlocks.sortedBy { it.centerY }
                    val cellText = sortedCellBlocks.joinToString("\n") { it.text.trim() }

                    val code = findCourseCodeInCell(cellText, courses) ?: continue
                    
                    val isLab = cellText.contains("(P)", ignoreCase = true) || cellText.contains("Lab", ignoreCase = true) || cellText.contains("LAB", ignoreCase = true)
                    val type = if (isLab) "P" else "L"

                    var facultyAbbr: String? = null
                    for (init in initialsList) {
                        val patternStr = "\\b$init\\b"
                        if (Pattern.compile(patternStr).matcher(cellText).find()) {
                            facultyAbbr = init
                            break
                        }
                    }

                    var remainingText = cellText
                    remainingText = remainingText.replace("(?i)\\b${code}\\b".toRegex(), "").trim()
                    
                    if (facultyAbbr != null) {
                        remainingText = remainingText.replace("\\b$facultyAbbr\\b".toRegex(), "").trim()
                    }
                    
                    remainingText = remainingText.replace("\\([LP]\\)".toRegex(), "")
                        .replace("\\b[LP]\\b".toRegex(), "")
                        .trim()

                    if (facultyAbbr == null) {
                        val matchingCourse = courses.find { it.code.equals(code, ignoreCase = true) }
                        if (matchingCourse != null && matchingCourse.faculty.isNotEmpty()) {
                            facultyAbbr = matchingCourse.faculty.first()
                        }
                    }

                    val facultyName = if (facultyAbbr != null) {
                        facultyMap[facultyAbbr] ?: facultyAbbr
                    } else {
                        "N/A"
                    }

                    var group: String? = null
                    val groupPattern = Pattern.compile("\\((Group\\s*[A-B])\\)", Pattern.CASE_INSENSITIVE)
                    val groupMatcher = groupPattern.matcher(remainingText)
                    if (groupMatcher.find()) {
                        group = groupMatcher.group(1)
                        val startIdx = groupMatcher.start()
                        val endIdx = groupMatcher.end()
                        remainingText = (remainingText.substring(0, startIdx).trim() + " " + remainingText.substring(endIdx).trim()).trim()
                    }

                    var location: String? = remainingText.replace("\n", " ").trim()
                    if (location != null) {
                        while (true) {
                            val prev = location!!
                            location = location!!.trim()
                                .removePrefix(",").removePrefix(".").removePrefix("-")
                                .removeSuffix(",").removeSuffix(".").removeSuffix("-")
                                .trim()
                            if (location == prev) break
                        }
                        if (location!!.isEmpty()) location = null
                    }

                    var startSlot = slotIdx
                    var endSlot = slotIdx
                    if (isLab) {
                        if (slotIdx == 2) { 
                            startSlot = 1
                            endSlot = 3
                        } else if (slotIdx == 7) { 
                            startSlot = 6
                            endSlot = 8
                        } else if (slotIdx == 1) { 
                            startSlot = 0
                            endSlot = 2
                        }
                    }

                    val startPart = splitTimeSlot(slots[startSlot]).first
                    val endPart = splitTimeSlot(slots[endSlot]).second
                    val timeRange = "$startPart-$endPart"
                    
                    schedule.add(
                        ScheduleEvent(
                            day = dayName,
                            time = timeRange,
                            start_time = startPart,
                            end_time = endPart,
                            course_code = code,
                            type = type,
                            location = location,
                            group = group,
                            faculty_abbr = facultyAbbr,
                            faculty_name = facultyName
                        )
                    )
                }

                val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
                val sortedSchedule = schedule.sortedWith(
                    compareBy(
                        { dayOrder.indexOf(it.day).let { idx -> if (idx == -1) 99 else idx } },
                        { it.start_time }
                    )
                )

                return TimetableData(
                    semester = semesterName,
                    faculty = facultyMap,
                    courses = courses,
                    schedule = sortedSchedule
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseTimetablePage failed: ${e.message}", e)
            return TimetableData(semester = "Error parsing page: ${e.localizedMessage}")
        }
    }
}
