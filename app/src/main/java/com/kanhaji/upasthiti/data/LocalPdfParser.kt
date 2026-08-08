package com.kanhaji.upasthiti.data

import android.util.Log
import com.kanhaji.upasthiti.features.home.domain.model.CourseInfo
import com.kanhaji.upasthiti.features.home.domain.model.DetectedTimetable
import com.kanhaji.upasthiti.features.home.domain.model.ScheduleEvent
import com.kanhaji.upasthiti.features.home.domain.model.TimetableData
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

    /** Canonical day names in display order. */
    private val CANONICAL_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

    /** Default fallback time slots if dynamic detection fails. */
    private val DEFAULT_SLOTS = listOf(
        "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00", "12:00-13:00",
        "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"
    )

    // ─── Public helper methods ───────────────────────────────────

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

    /**
     * Match a raw day-name string to a canonical day name using prefix matching.
     * Handles truncated names like "Wednesda" or case variations.
     * Returns null if no match found.
     */
    private fun matchDay(rawDay: String): String? {
        val cleaned = rawDay.replace("\n", "").trim()
        if (cleaned.length < 3) return null
        val lower = cleaned.lowercase()
        return CANONICAL_DAYS.find { canonical ->
            canonical.lowercase().startsWith(lower) || lower.startsWith(canonical.lowercase().take(3))
        }
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
            cleaned = cleaned.replace("\\b${Pattern.quote(init)}\\b".toRegex(), "")
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

    /**
     * Find a course code in cell text by matching against known courses first,
     * then falling back to regex pattern.
     * Uses Pattern.quote() for safe regex interpolation.
     */
    private fun findCourseCodeInCell(cellText: String, courses: List<CourseInfo>): String? {
        // First: match against known course codes from the legend
        for (c in courses) {
            val pattern = Pattern.compile("\\b${Pattern.quote(c.code)}\\b", Pattern.CASE_INSENSITIVE)
            if (pattern.matcher(cellText).find()) {
                return c.code
            }
        }
        // Fallback: regex match for course code patterns — but only if we have no course list
        if (courses.isEmpty()) {
            val m = Pattern.compile("\\b(?:[A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\b", Pattern.CASE_INSENSITIVE).matcher(cellText)
            if (m.find()) {
                return m.group(0)!!
            }
        }
        return null
    }

    /**
     * Check if any page in the document contains timetable indicators.
     * Scans all pages, not just page 1.
     */
    fun isLikelyTimetable(fileBytes: ByteArray): Boolean {
        try {
            PDDocument.load(ByteArrayInputStream(fileBytes)).use { document ->
                if (document.numberOfPages == 0) return false
                val pagesToCheck = Math.min(document.numberOfPages, 3)
                for (p in 1..pagesToCheck) {
                    val stripper = TimetableStripper()
                    stripper.startPage = p
                    stripper.endPage = p
                    stripper.getText(document)
                    val text = stripper.charList.map { it.char }.joinToString("").lowercase()

                    val hasDays = CANONICAL_DAYS.any { text.contains(it.lowercase()) }
                    val hasKeywords = listOf("time table", "timetable", "schedule", "semester").any { text.contains(it) }
                    val hasTimeSlots = Pattern.compile("\\d{2}:\\d{2}").matcher(text).find()

                    if (hasDays && (hasKeywords || hasTimeSlots)) return true
                }
                return false
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

    fun calculateSha256(bytes: ByteArray): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            bytes.contentHashCode().toString()
        }
    }

    // ─── Main parser ─────────────────────────────────────────────

    fun parseTimetablePage(fileBytes: ByteArray, pageIdx: Int): TimetableData {
        Log.d(TAG, "parseTimetablePage: Starting parse for page index: $pageIdx")
        try {
            PDDocument.load(ByteArrayInputStream(fileBytes)).use { document ->

                // ── Step 1: Extract characters via TimetableStripper ──
                val stripper = TimetableStripper()
                stripper.startPage = pageIdx + 1
                stripper.endPage = pageIdx + 1
                stripper.getText(document)
                Log.d(TAG, "parseTimetablePage: Extracted ${stripper.charList.size} characters")

                // ── Step 2: Group characters into horizontal lines and then TextBlocks ──
                val blocks = buildTextBlocks(stripper.charList)
                Log.d(TAG, "parseTimetablePage: Built ${blocks.size} text blocks")

                // ── Step 3: Extract vector lines from content stream ──
                val page = document.getPage(pageIdx)
                val lineExtractor = PdfLineExtractor(page)
                val gridLines = lineExtractor.extract()

                val hasVectorGrid = gridLines.horizontal.size >= 5 && gridLines.vertical.size >= 5

                if (hasVectorGrid) {
                    Log.d(TAG, "parseTimetablePage: Vector grid detected (${gridLines.horizontal.size}H + ${gridLines.vertical.size}V lines) → using grid-first path")
                    return parseWithVectorGrid(document, pageIdx, blocks, gridLines, stripper, fileBytes)
                } else {
                    Log.w(TAG, "parseTimetablePage: No vector grid found → falling back to text-heuristic path")
                    return parseWithTextHeuristic(document, pageIdx, blocks, stripper, fileBytes)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseTimetablePage failed: ${e.message}", e)
            return TimetableData(semester = "Error parsing page: ${e.localizedMessage}")
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ██ GRID-FIRST PARSE PATH (PRIMARY — using vector lines) ██
    // ═══════════════════════════════════════════════════════════

    private fun parseWithVectorGrid(
        document: PDDocument,
        pageIdx: Int,
        blocks: List<TextBlock>,
        gridLines: PdfGridLines,
        stripper: TimetableStripper,
        fileBytes: ByteArray
    ): TimetableData {

        // ── Step 1: Identify timetable grid region ──
        // The outer vertical border lines span exactly the timetable grid height.
        // Use the bottom of the longest vertical lines to determine the table extent.
        val vLineLengths = gridLines.vertical.map { Math.abs(it.y1 - it.y0) }
        val maxVLineLength = vLineLengths.maxOrNull() ?: 0f

        // Find the bottom Y of vertical lines that are at least 80% of the max height
        // (these are the outer table borders)
        val outerBorderBottoms = gridLines.vertical
            .filter { Math.abs(it.y1 - it.y0) >= maxVLineLength * 0.8f }
            .map { Math.max(it.y0, it.y1) }

        val tableBottomY = if (outerBorderBottoms.isNotEmpty()) {
            outerBorderBottoms.average().toFloat()
        } else {
            gridLines.horizontal.maxOf { it.hY }
        }

        Log.d(TAG, "parseWithVectorGrid: Table bottom Y = $tableBottomY")

        // ── Step 2: Build the grid structure ──
        val grid = GridCellResolver.buildGrid(gridLines, tableBottomY)
        if (grid == null) {
            Log.w(TAG, "parseWithVectorGrid: Grid building failed → falling back to text-heuristic")
            return parseWithTextHeuristic(document, pageIdx, blocks, stripper, fileBytes)
        }

        Log.d(TAG, "parseWithVectorGrid: Grid has ${grid.rowEdges.size - 1} rows × ${grid.colEdges.size - 1} cols")

        // ── Step 3: Assign text blocks to grid cells ──
        val cellTextMap = GridCellResolver.assignTextToGrid(grid, blocks)

        // ── Step 4: Identify header row (row 0) → map columns to time slots ──
        val headerCells = grid.cells.filter { (key, _) -> key.first == 0 }
        val slotTimePattern = Pattern.compile("\\d{2}:\\d{2}[^\\d]{0,5}\\d{2}:\\d{2}")

        // Build column → time-slot mapping from header row
        data class TimeSlot(val label: String, val startTime: String, val endTime: String)
        val colToSlot = mutableMapOf<Int, TimeSlot>()

        for ((key, cell) in headerCells) {
            val textInCell = cellTextMap[key]
                ?.sortedBy { it.minY }
                ?.joinToString(" ") { it.text.trim() }
                ?: continue

            val m = slotTimePattern.matcher(textInCell)
            if (m.find()) {
                val raw = m.group(0)!!
                    .replace("\\s+".toRegex(), "")
                    .replace("[^0-9:-]".toRegex(), "-")
                val (start, end) = splitTimeSlot(raw)

                // This header cell might span multiple columns (shouldn't normally, but handle it)
                for (c in cell.col until cell.col + cell.colSpan) {
                    colToSlot[c] = TimeSlot(raw, start, end)
                }
            }
        }

        Log.d(TAG, "parseWithVectorGrid: Time slot mapping: $colToSlot")


        // ── Step 5: Identify day column (col 0 or col 1) → map rows to days ──
        val rowToDay = mutableMapOf<Int, String>()
        // Try first two columns for day names
        for (tryCol in 0..1) {
            for ((key, _) in grid.cells.filter { it.key.second == tryCol && it.key.first > 0 }) {
                val textInCell = cellTextMap[key]
                    ?.joinToString(" ") { it.text.trim() }
                    ?: continue
                val day = matchDay(textInCell)
                if (day != null) {
                    rowToDay[key.first] = day
                }
            }
            if (rowToDay.isNotEmpty()) break
        }

        // Also try matching day names from all blocks (more robust)
        if (rowToDay.isEmpty()) {
            for (b in blocks) {
                val day = matchDay(b.text.trim())
                if (day != null) {
                    // Find which row this block falls in
                    for (r in 0 until grid.rowEdges.size - 1) {
                        if (b.centerY >= grid.rowEdges[r] - 2f && b.centerY <= grid.rowEdges[r + 1] + 2f) {
                            rowToDay[r] = day
                            break
                        }
                    }
                }
            }
        }

        Log.d(TAG, "parseWithVectorGrid: Day mapping: $rowToDay")

        // ── Step 6: Extract semester title ──
        val semesterName = extractSemesterTitle(blocks, grid.tableTop, stripper)

        // ── Step 7: Parse legend (faculty + courses) from below the table ──
        val legendBlocks = blocks.filter { it.centerY > tableBottomY + 2f }
        val (facultyMap, initialsList, courses) = parseLegend(legendBlocks)

        Log.d(TAG, "parseWithVectorGrid: Found ${courses.size} courses, ${facultyMap.size} faculty mappings")

        // ── Step 8: Build schedule events from grid cells ──
        val schedule = mutableListOf<ScheduleEvent>()

        // Determine which columns are the "day label" column(s) vs data columns
        val dayCol = if (rowToDay.isNotEmpty()) {
            // The day labels are usually in the first data column
            grid.cells.filter { rowToDay.containsKey(it.key.first) && it.key.second <= 1 }
                .keys.firstOrNull()?.second ?: 0
        } else 0

        for ((key, cell) in grid.cells) {
            val row = key.first
            val col = key.second

            // Skip header row and day-label column
            if (row == 0 || col <= dayCol) continue

            // Determine day for this row
            // For merged rows, find the day from the topmost row of the cell
            var dayName: String? = null
            for (r in cell.row downTo 1) {
                if (rowToDay.containsKey(r)) {
                    dayName = rowToDay[r]
                    break
                }
            }
            if (dayName == null) continue

            // Get text content of this cell
            val cellBlocks = cellTextMap[key] ?: continue
            val cellText = cellBlocks.sortedBy { it.minY }.joinToString("\n") { it.text.trim() }
            if (cellText.isBlank() || cellText.equals("LUNCH", ignoreCase = true)) continue

            // Find course code
            val code = findCourseCodeInCell(cellText, courses) ?: continue

            // Determine time range from column span
            val startCol = cell.col
            val endCol = cell.col + cell.colSpan - 1

            val startSlot = colToSlot[startCol]
            val endSlot = colToSlot[endCol]

            if (startSlot == null || endSlot == null) {
                Log.w(TAG, "parseWithVectorGrid: No time slot for cols $startCol..$endCol, cell text: $cellText")
                continue
            }

            val startTime = startSlot.startTime
            val endTime = endSlot.endTime
            val timeRange = "$startTime-$endTime"

            // Determine type: use course info from legend or cell markers
            val courseInfo = courses.find { it.code.equals(code, ignoreCase = true) }
            val isLab = courseInfo?.details?.contains("P", ignoreCase = true) == true ||
                    cellText.contains("(P)", ignoreCase = true) ||
                    cellText.contains("Lab", ignoreCase = true) ||
                    cell.colSpan >= 2  // Multi-column cells are labs
            val type = if (isLab) "P" else "L"

            // Extract faculty abbreviation:
            // 1. PRIMARY: use subject→teacher mapping from legend (most reliable)
            // 2. FALLBACK: scan cell text for known initials
            var facultyAbbr: String? = null

            // Primary: legend says which teacher teaches this subject
            if (courseInfo != null && courseInfo.faculty.isNotEmpty()) {
                facultyAbbr = courseInfo.faculty.first()
            }

            // Fallback: scan cell text for known initials
            if (facultyAbbr == null) {
                for (init in initialsList) {
                    val pat = Pattern.compile("\\b${Pattern.quote(init)}\\b")
                    if (pat.matcher(cellText).find()) {
                        facultyAbbr = init
                        break
                    }
                }
            }

            val facultyName = if (facultyAbbr != null) {
                facultyMap[facultyAbbr] ?: facultyAbbr
            } else "N/A"

            // Extract group
            var group: String? = null
            val groupPattern = Pattern.compile("\\((Group\\s*[A-Z])\\)", Pattern.CASE_INSENSITIVE)
            val groupMatcher = groupPattern.matcher(cellText)
            if (groupMatcher.find()) {
                group = groupMatcher.group(1)
            }

            // Extract location (remaining text after removing code, ALL faculty initials, group, type markers)
            var location = extractLocation(cellText, code, facultyAbbr, group, initialsList)

            val event = ScheduleEvent(
                day = dayName,
                time = timeRange,
                start_time = startTime,
                end_time = endTime,
                course_code = code,
                type = type,
                location = location,
                group = group,
                faculty_abbr = facultyAbbr,
                faculty_name = facultyName
            )
            Log.d(TAG, "parseWithVectorGrid: EVENT → ${event.day} ${event.time} [${event.course_code}] type=${event.type} loc=${event.location} grp=${event.group} fac=${event.faculty_abbr} colSpan=${cell.colSpan}")
            schedule.add(event)
        }

        Log.d(TAG, "parseWithVectorGrid: TOTAL EVENTS: ${schedule.size}")

        return finalizeTimetable(schedule, semesterName, facultyMap, courses, fileBytes, pageIdx)
    }

    // ═══════════════════════════════════════════════════════════
    // ██ TEXT-HEURISTIC FALLBACK PATH (for PDFs without lines) ██
    // ═══════════════════════════════════════════════════════════

    private fun parseWithTextHeuristic(
        document: PDDocument,
        pageIdx: Int,
        blocks: List<TextBlock>,
        stripper: TimetableStripper,
        fileBytes: ByteArray
    ): TimetableData {
        // ── Day detection ──
        val dayYMap = mutableMapOf<String, Float>()
        for (b in blocks) {
            val day = matchDay(b.text.trim())
            if (day != null) {
                dayYMap[day] = b.centerY
            }
        }
        Log.d(TAG, "parseWithTextHeuristic: Day Y-coordinates: $dayYMap")

        val maxDayY = (dayYMap.values.maxOrNull() ?: 250f) + 15f

        // ── Time slot detection ──
        val slotTimePattern = Pattern.compile("\\d{2}:\\d{2}[^\\d]{1,5}\\d{2}:\\d{2}")
        val candidateHeaderBlocks = blocks.filter { it.centerY < maxDayY && (it.text.contains(":") || it.text.contains("Hrs")) }
            .sortedWith(compareBy({ it.centerY }, { it.minX }))

        val mergedHeaderBlocks = mutableListOf<TextBlock>()
        var skipNext = false
        for (i in candidateHeaderBlocks.indices) {
            if (skipNext) { skipNext = false; continue }
            val current = candidateHeaderBlocks[i]
            if (!slotTimePattern.matcher(current.text).find() && i + 1 < candidateHeaderBlocks.size) {
                val next = candidateHeaderBlocks[i + 1]
                if (Math.abs(current.centerY - next.centerY) < 6f && (next.minX - current.maxX) < 30f) {
                    val combinedText = "${current.text} ${next.text}"
                    if (slotTimePattern.matcher(combinedText).find()) {
                        mergedHeaderBlocks.add(TextBlock(combinedText, current.minX, next.maxX, Math.min(current.minY, next.minY), Math.max(current.maxY, next.maxY)))
                        skipNext = true
                        continue
                    }
                }
            }
            mergedHeaderBlocks.add(current)
        }

        val matchingHeaderBlocks = mergedHeaderBlocks.filter { slotTimePattern.matcher(it.text).find() }
        val headerRows = mutableListOf<MutableList<TextBlock>>()
        for (b in matchingHeaderBlocks) {
            var added = false
            for (row in headerRows) {
                if (Math.abs(b.centerY - row[0].centerY) < 6f) { row.add(b); added = true; break }
            }
            if (!added) headerRows.add(mutableListOf(b))
        }

        val slotHeaderRow = headerRows.maxByOrNull { it.size }?.sortedBy { it.centerX } ?: emptyList()
        val slots = mutableListOf<String>()
        val slotCenters = mutableListOf<Float>()

        if (slotHeaderRow.size >= 5) {
            for (b in slotHeaderRow) {
                val m = slotTimePattern.matcher(b.text)
                val timeStr = if (m.find()) m.group(0)!!.replace("\\s+".toRegex(), "").replace("[^0-9:-]".toRegex(), "-") else b.text
                slots.add(timeStr)
                slotCenters.add(b.centerX)
            }
        } else {
            slots.addAll(DEFAULT_SLOTS)
            // Use default centers (these are tuned for a specific PDF but better than nothing)
            slotCenters.addAll(listOf(108.2f, 172.0f, 249.7f, 336.0f, 406.0f, 480.0f, 550.0f, 625.0f, 704.0f, 790.0f))
        }

        // Gap interpolation
        interpolateTextSlotGaps(slots, slotCenters)

        fun getSlotIndex(centerX: Float): Int {
            var minDiff = Float.MAX_VALUE
            var slotIdx = -1
            for (i in slotCenters.indices) {
                val diff = Math.abs(centerX - slotCenters[i])
                if (diff < minDiff) { minDiff = diff; slotIdx = i }
            }
            return slotIdx
        }

        // ── Legend parsing ──
        val legendBlocks = blocks.filter { it.centerY >= maxDayY }
        val (facultyMap, initialsList, courses) = parseLegend(legendBlocks)

        // ── Cell assignment ──
        val cellGroupMap = mutableMapOf<String, MutableList<TextBlock>>()
        val dayBlocksMap = mutableMapOf<String, MutableList<TextBlock>>()

        for (b in blocks) {
            if (b.centerY >= maxDayY) continue
            val text = b.text.trim()
            if (text == "LUNCH" || matchDay(text) != null ||
                text.startsWith("Time Table") || text.contains("Hrs.") ||
                text.contains("08:00") || text.contains("Semester")) continue

            var closestDay = ""
            val sortedDays = dayYMap.toList().sortedBy { it.second }
            for (i in sortedDays.indices) {
                val (day, yVal) = sortedDays[i]
                val nextYVal = if (i + 1 < sortedDays.size) sortedDays[i + 1].second else Float.MAX_VALUE
                if (b.centerY >= (yVal - 4.0f) && b.centerY < (nextYVal - 4.0f)) {
                    closestDay = day; break
                }
            }
            if (closestDay.isNotEmpty()) {
                dayBlocksMap.getOrPut(closestDay) { mutableListOf() }.add(b)
            }
        }

        for ((dayName, dayBlocks) in dayBlocksMap) {
            val sortedDayBlocks = dayBlocks.sortedBy { it.centerY }
            val cellClusters = mutableListOf<MutableList<TextBlock>>()
            for (b in sortedDayBlocks) {
                var added = false
                for (cluster in cellClusters) {
                    val primary = cluster.find { findCourseCodeInCell(it.text, courses) != null } ?: cluster[0]
                    if (Math.abs(b.centerX - primary.centerX) < 45f && Math.abs(b.centerY - cluster.map { it.centerY }.average()) < 35f) {
                        cluster.add(b); added = true; break
                    }
                }
                if (!added) cellClusters.add(mutableListOf(b))
            }

            for (cluster in cellClusters) {
                val primary = cluster.find { findCourseCodeInCell(it.text, courses) != null } ?: cluster.minByOrNull { it.centerY } ?: cluster[0]
                val colIdx = getSlotIndex(primary.centerX)
                if (colIdx != -1) {
                    val key = "$dayName:$colIdx"
                    cellGroupMap.getOrPut(key) { mutableListOf() }.addAll(cluster)
                }
            }
        }

        // ── Event creation ──
        val schedule = mutableListOf<ScheduleEvent>()

        for ((key, cellBlocks) in cellGroupMap) {
            val parts = key.split(":")
            val dayName = parts[0]
            val slotIdx = parts[1].toInt()

            val sortedCellBlocks = cellBlocks.sortedBy { it.centerY }
            val cellText = sortedCellBlocks.joinToString("\n") { it.text.trim() }

            val code = findCourseCodeInCell(cellText, courses) ?: continue

            val courseInfo = courses.find { it.code.equals(code, ignoreCase = true) }
            val isLab = courseInfo?.details?.contains("P", ignoreCase = true) == true ||
                    cellText.contains("(P)", ignoreCase = true) ||
                    cellText.contains("Lab", ignoreCase = true) ||
                    cellText.contains("LAB", ignoreCase = true)
            val type = if (isLab) "P" else "L"

            var facultyAbbr: String? = null
            for (init in initialsList) {
                if (Pattern.compile("\\b${Pattern.quote(init)}\\b").matcher(cellText).find()) {
                    facultyAbbr = init; break
                }
            }

            if (facultyAbbr == null && courseInfo != null && courseInfo.faculty.isNotEmpty()) {
                facultyAbbr = courseInfo.faculty.first()
            }

            val facultyName = if (facultyAbbr != null) facultyMap[facultyAbbr] ?: facultyAbbr else "N/A"

            var group: String? = null
            val groupPattern = Pattern.compile("\\((Group\\s*[A-Z])\\)", Pattern.CASE_INSENSITIVE)
            val groupMatcher = groupPattern.matcher(cellText)
            if (groupMatcher.find()) group = groupMatcher.group(1)

            val location = extractLocation(cellText, code, facultyAbbr, group)

            var startSlot = slotIdx
            var endSlot = slotIdx
            if (isLab) {
                fun isSlotOccupied(day: String, sIdx: Int): Boolean {
                    val k = "$day:$sIdx"
                    val blocksInSlot = cellGroupMap[k] ?: return false
                    val joinedText = blocksInSlot.joinToString(" ") { it.text.trim() }.trim()
                    return joinedText.isNotEmpty() && (findCourseCodeInCell(joinedText, courses) != null || joinedText.contains("LUNCH", ignoreCase = true))
                }
                var maxS = slotIdx
                while (maxS > 0 && !isSlotOccupied(dayName, maxS - 1) && !slots[maxS - 1].contains("LUNCH", ignoreCase = true)) maxS--
                var maxE = slotIdx
                while (maxE + 1 < slots.size && !isSlotOccupied(dayName, maxE + 1) && !slots[maxE + 1].contains("LUNCH", ignoreCase = true)) maxE++
                startSlot = maxS
                endSlot = maxE
            }

            val startPart = splitTimeSlot(slots[startSlot]).first
            val endPart = splitTimeSlot(slots[endSlot]).second
            val timeRange = "$startPart-$endPart"

            schedule.add(ScheduleEvent(
                day = dayName, time = timeRange, start_time = startPart, end_time = endPart,
                course_code = code, type = type, location = location, group = group,
                faculty_abbr = facultyAbbr, faculty_name = facultyName
            ))
        }

        val semesterName = extractSemesterTitle(blocks, null, stripper)
        return finalizeTimetable(schedule, semesterName, facultyMap, courses, fileBytes, pageIdx)
    }

    // ═══════════════════════════════════════════════════════════
    // ██ SHARED HELPER METHODS                               ██
    // ═══════════════════════════════════════════════════════════

    /**
     * Group characters into horizontal text lines, then split each line
     * into TextBlocks at horizontal gaps.
     */
    private fun buildTextBlocks(charList: List<CharPos>): List<TextBlock> {
        val sortedChars = charList.sortedWith(compareBy({ it.y }, { it.x }))
        val lines = mutableListOf<MutableList<CharPos>>()

        // Compute adaptive Y-gap threshold from median character height
        val medianHeight = if (charList.isNotEmpty()) {
            charList.map { it.height }.sorted().let { it[it.size / 2] }
        } else 10f
        val yGapThreshold = medianHeight * 0.5f  // Half the median char height

        for (char in sortedChars) {
            var added = false
            for (line in lines) {
                if (Math.abs(char.y - line[0].y) < yGapThreshold) {
                    line.add(char)
                    added = true
                    break
                }
            }
            if (!added) {
                lines.add(mutableListOf(char))
            }
        }

        // Compute adaptive X-gap threshold from median character width
        val medianWidth = if (charList.isNotEmpty()) {
            charList.map { it.width }.filter { it > 0 }.sorted().let {
                if (it.isNotEmpty()) it[it.size / 2] else 3.5f
            }
        } else 3.5f
        val xGapThreshold = medianWidth * 0.7f  // 70% of median char width

        val blocks = mutableListOf<TextBlock>()
        for (line in lines) {
            val sortedLine = line.sortedBy { it.x }
            var currentBlock = StringBuilder()
            var minX = 0f; var maxX = 0f; var minY = 0f; var maxY = 0f

            for (i in sortedLine.indices) {
                val c = sortedLine[i]
                if (currentBlock.isEmpty()) {
                    currentBlock.append(c.char)
                    minX = c.x; maxX = c.x + c.width; minY = c.y; maxY = c.y + c.height
                } else {
                    val prev = sortedLine[i - 1]
                    val gap = c.x - (prev.x + prev.width)
                    if (gap < xGapThreshold) {
                        currentBlock.append(c.char)
                        maxX = c.x + c.width
                        minY = Math.min(minY, c.y)
                        maxY = Math.max(maxY, c.y + c.height)
                    } else {
                        blocks.add(TextBlock(currentBlock.toString(), minX, maxX, minY, maxY))
                        currentBlock = StringBuilder().append(c.char)
                        minX = c.x; maxX = c.x + c.width; minY = c.y; maxY = c.y + c.height
                    }
                }
            }
            if (currentBlock.isNotEmpty()) {
                blocks.add(TextBlock(currentBlock.toString(), minX, maxX, minY, maxY))
            }
        }
        return blocks
    }

    /**
     * Extract semester title from blocks near the top of the page.
     * Uses positional detection (blocks above the table top) instead of
     * institute name blacklists.
     */
    private fun extractSemesterTitle(
        blocks: List<TextBlock>,
        tableTop: Float?,
        stripper: TimetableStripper
    ): String {
        // First try: regex match on full text
        val fullText = stripper.charList.map { it.char }.joinToString("")
        val semPattern = Pattern.compile("(\\d+(?:st|nd|rd|th)?\\s+Semester)", Pattern.CASE_INSENSITIVE)
        val semMatcher = semPattern.matcher(fullText)
        if (semMatcher.find()) {
            // Try to get more context — look for "MCA" or program name before it
            val matchStart = semMatcher.start()
            val contextStart = Math.max(0, matchStart - 30)
            val context = fullText.substring(contextStart, semMatcher.end()).trim()

            // Extract just the program + semester part
            val fullPattern = Pattern.compile("([A-Za-z.]+\\s+\\d+(?:st|nd|rd|th)?\\s+Semester)", Pattern.CASE_INSENSITIVE)
            val fullMatcher = fullPattern.matcher(context)
            return if (fullMatcher.find()) {
                fullMatcher.group(1)!!.trim()
            } else {
                semMatcher.group(1)!!.trim()
            }
        }

        // Fallback: find semester text among top blocks (above the table)
        val topCutoff = tableTop ?: blocks.map { it.centerY }.sorted().let {
            if (it.size > 5) it[5] else 100f
        }
        val topBlocks = blocks.filter { it.centerY < topCutoff }
        for (b in topBlocks) {
            if (b.text.contains("Semester", ignoreCase = true) || b.text.contains("Sem", ignoreCase = true)) {
                return b.text.trim()
            }
        }

        return "Unknown Semester"
    }

    /**
     * Parse the legend section (below the timetable grid) to extract:
     * - Faculty abbreviation → full name mappings
     * - Course code → course info mappings
     *
     * Uses dynamic gap detection instead of hardcoded `centerX < 390f`.
     */
    private fun parseLegend(
        legendBlocks: List<TextBlock>
    ): Triple<Map<String, String>, List<String>, List<CourseInfo>> {
        if (legendBlocks.isEmpty()) return Triple(emptyMap(), emptyList(), emptyList())

        // Dynamic column split: find the largest horizontal gap in legend text X-positions
        val sortedByX = legendBlocks.sortedBy { it.centerX }
        var maxGap = 0f
        var splitX = Float.MAX_VALUE

        for (i in 0 until sortedByX.size - 1) {
            val gap = sortedByX[i + 1].minX - sortedByX[i].maxX
            if (gap > maxGap) {
                maxGap = gap
                splitX = (sortedByX[i].maxX + sortedByX[i + 1].minX) / 2f
            }
        }

        // If no significant gap found, use page midpoint
        if (maxGap < 20f) {
            val allX = legendBlocks.map { it.centerX }
            splitX = (allX.min() + allX.max()) / 2f
        }

        Log.d(TAG, "parseLegend: Column split at X = $splitX (gap = $maxGap)")

        val leftBlocks = legendBlocks.filter { it.centerX < splitX }
            .sortedWith(compareBy({ it.centerY }, { it.minX }))
        val rightBlocks = legendBlocks.filter { it.centerX >= splitX }
            .sortedWith(compareBy({ it.centerY }, { it.minX }))

        val leftText = leftBlocks.joinToString(" ") { it.text }
        val rightText = rightBlocks.joinToString(" ") { it.text }

        Log.d(TAG, "parseLegend: LEFT text: $leftText")
        Log.d(TAG, "parseLegend: RIGHT text: $rightText")

        // Parse faculty names
        val nameList = mutableListOf<String>()
        val namePattern = Pattern.compile("(Prof\\.|Dr\\.|Mr\\.|Mrs\\.)\\s+[A-Z][a-zA-Z.]+(?:\\s+[A-Z][a-zA-Z.]+)*")
        for (text in listOf(leftText, rightText)) {
            val nameMatcher = namePattern.matcher(text)
            while (nameMatcher.find()) {
                nameList.add(nameMatcher.group(0)!!.trim().replace("\\s+".toRegex(), " "))
            }
        }

        // Parse faculty initials
        val initialsList = mutableListOf<String>()
        val initialsPattern = Pattern.compile("([A-Z]{2,5})\\s*:")
        for (text in listOf(leftText, rightText)) {
            val initialsMatcher = initialsPattern.matcher(text)
            while (initialsMatcher.find()) {
                initialsList.add(initialsMatcher.group(1)!!.trim())
            }
        }

        // Build faculty map
        val facultyMap = mutableMapOf<String, String>()
        for (init in initialsList) {
            val matchName = nameList.find { matchInitialsToName(init, it) }
            if (matchName != null) {
                facultyMap[init] = matchName
            } else {
                var resolved = false
                for (text in listOf(leftText, rightText)) {
                    val fallbackPattern = Pattern.compile("\\b${Pattern.quote(init)}\\s*:\\s*(Prof\\.|Dr\\.|Mr\\.|Mrs\\.)?\\s*([^,:]+)")
                    val fallbackMatcher = fallbackPattern.matcher(text)
                    if (fallbackMatcher.find()) {
                        val title = fallbackMatcher.group(1)?.trim() ?: ""
                        val restName = fallbackMatcher.group(2)?.trim() ?: ""
                        val cleanName = (if (title.isNotEmpty()) "$title " else "") + restName.replace("\n", " ").trim()
                        facultyMap[init] = cleanName.replace("\\s+".toRegex(), " ").trim()
                        resolved = true
                        break
                    }
                }
                if (!resolved) facultyMap[init] = init
            }
        }

        // Parse courses — single unified pattern
        val courses = mutableListOf<CourseInfo>()
        val courseCodePattern = Pattern.compile("\\b([A-Z]+\\d+[A-Z0-9\\-]*|PE-[IVXLCDM]+)\\b")

        // Detect where the faculty definition block starts (e.g., "RT: Mr. Rajesh")
        // so we don't include it in the last course's text segment.
        val facDefPattern = Pattern.compile("\\b[A-Z]{2,5}\\s*:\\s*(?:Prof\\.|Dr\\.|Mr\\.|Mrs\\.)")

        for (text in listOf(leftText, rightText)) {
            // Find where faculty definitions start — truncate course parsing there
            val facDefMatcher = facDefPattern.matcher(text)
            val courseTextEnd = if (facDefMatcher.find()) facDefMatcher.start() else text.length

            // Find all course code positions (only within the course section)
            val codePositions = mutableListOf<Pair<Int, String>>() // (startIndex, code)
            val codeMatcher = courseCodePattern.matcher(text.substring(0, courseTextEnd))
            while (codeMatcher.find()) {
                val candidate = codeMatcher.group(1)!!
                // Skip if it's an initials abbreviation or a day name
                if (initialsList.any { it.equals(candidate, ignoreCase = true) }) continue
                if (CANONICAL_DAYS.any { it.equals(candidate, ignoreCase = true) }) continue
                if (candidate == "LUNCH") continue
                codePositions.add(codeMatcher.start() to candidate)
            }

            val courseSection = text.substring(0, courseTextEnd)

            // For each code, extract text between this code and the next code
            for (i in codePositions.indices) {
                val (pos, code) = codePositions[i]
                val endPos = if (i + 1 < codePositions.size) codePositions[i + 1].first else courseSection.length
                val segment = courseSection.substring(pos + code.length, endPos).trim()

                // Check if this looks like a course entry (has = or ( after the code)
                val isEqualsFormat = segment.startsWith("=") || segment.startsWith(" =")
                val isParenFormat = segment.startsWith("(") || segment.startsWith(" (")

                if (!isEqualsFormat && !isParenFormat) continue
                if (courses.any { it.code.equals(code, ignoreCase = true) }) continue

                // Extract details from parentheses
                var details = ""
                val detailsMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(segment)
                if (detailsMatcher.find()) {
                    details = detailsMatcher.group(1)!!.trim()
                }

                // Extract faculty refs — only match initials that appear as standalone
                // words in the course segment (not inside parenthetical details)
                val facList = mutableListOf<String>()
                for (init in initialsList) {
                    if (Pattern.compile("\\b${Pattern.quote(init)}\\b").matcher(segment).find()) {
                        facList.add(init)
                    }
                }

                val rawName = segment.removePrefix("=").trim()
                val name = cleanCourseName(rawName, initialsList)

                courses.add(CourseInfo(code, name, details, facList))
            }
        }

        Log.d(TAG, "parseLegend: Courses parsed: ${courses.map { "${it.code} (${it.details}) fac=${it.faculty}" }}")
        Log.d(TAG, "parseLegend: Faculty map: $facultyMap")
        return Triple(facultyMap, initialsList, courses)
    }

    /**
     * Extract location/room from cell text after removing known tokens.
     * Strips the course code, ALL known faculty initials, group, and type markers.
     */
    private fun extractLocation(
        cellText: String,
        code: String,
        facultyAbbr: String?,
        group: String?,
        allInitials: List<String> = emptyList()
    ): String? {
        var remaining = cellText
        remaining = remaining.replace("(?i)\\b${Pattern.quote(code)}\\b".toRegex(), "").trim()

        // Strip ALL known faculty initials (not just the matched one)
        for (init in allInitials) {
            remaining = remaining.replace("\\b${Pattern.quote(init)}\\b".toRegex(), "").trim()
        }
        // Also strip the specific matched one in case it wasn't in the list
        if (facultyAbbr != null) {
            remaining = remaining.replace("\\b${Pattern.quote(facultyAbbr)}\\b".toRegex(), "").trim()
        }

        remaining = remaining.replace("\\([LP]\\)".toRegex(), "")
            .replace("\\b[LP]\\b".toRegex(), "")

        if (group != null) {
            remaining = remaining.replace(Pattern.quote("($group)").toRegex(), "")
                .replace(Pattern.quote(group).toRegex(), "")
        }

        remaining = remaining.replace("\n", " ").trim()

        while (true) {
            val prev = remaining
            remaining = remaining.trim()
                .removePrefix(",").removePrefix(".").removePrefix("-")
                .removeSuffix(",").removeSuffix(".").removeSuffix("-")
                .trim()
            if (prev == remaining) break
        }

        return remaining.ifEmpty { null }
    }

    /**
     * Interpolate missing time slots for the text-heuristic path.
     */
    private fun interpolateTextSlotGaps(slots: MutableList<String>, slotCenters: MutableList<Float>) {
        fun parseStartMin(s: String): Int {
            val t = s.split("[-–—]".toRegex()).firstOrNull()?.trim() ?: ""
            val p = t.split(":"); return if (p.size == 2) (p[0].toIntOrNull() ?: -1) * 60 + (p[1].toIntOrNull() ?: -1) else -1
        }
        fun parseEndMin(s: String): Int {
            val parts = s.split("[-–—]".toRegex()); val t = if (parts.size >= 2) parts[1].trim() else ""
            val p = t.split(":"); return if (p.size == 2) (p[0].toIntOrNull() ?: -1) * 60 + (p[1].toIntOrNull() ?: -1) else -1
        }
        fun fmtTime(m: Int) = "%02d:%02d".format(m / 60, m % 60)

        var i = 0
        while (i < slots.size - 1) {
            val curEnd = parseEndMin(slots[i])
            val nextStart = parseStartMin(slots[i + 1])
            if (curEnd > 0 && nextStart > curEnd) {
                val gap = nextStart - curEnd
                if (gap in 30..120) {
                    val missing = "${fmtTime(curEnd)}-${fmtTime(nextStart)}"
                    val interpCenter = (slotCenters[i] + slotCenters[i + 1]) / 2f
                    slots.add(i + 1, missing)
                    slotCenters.add(i + 1, interpCenter)
                    Log.d(TAG, "interpolateTextSlotGaps: Inserted '$missing' at index ${i + 1}")
                }
            }
            i++
        }
    }

    /**
     * Sort events, compute hash, stamp ID, and return final TimetableData.
     */
    private fun finalizeTimetable(
        schedule: List<ScheduleEvent>,
        semesterName: String,
        facultyMap: Map<String, String>,
        courses: List<CourseInfo>,
        fileBytes: ByteArray,
        pageIdx: Int
    ): TimetableData {
        val dayOrder = CANONICAL_DAYS
        val sortedSchedule = schedule.sortedWith(
            compareBy(
                { dayOrder.indexOf(it.day).let { idx -> if (idx == -1) 99 else idx } },
                { it.start_time }
            )
        )

        Log.d(TAG, "finalizeTimetable: ${sortedSchedule.size} events for '$semesterName'")

        val pdfHash = calculateSha256(fileBytes)
        val unstamped = TimetableData(
            semester = semesterName,
            faculty = facultyMap,
            courses = courses,
            schedule = sortedSchedule,
            pdfHash = pdfHash,
            pageIndex = pageIdx
        )
        return TimeTableManager.stampTimetableId(unstamped)
    }
}
