package com.kanhaji.upastithi.features.home.ui.pages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.composables.RadioItem
import com.kanhaji.basics.composables.RadioSelectionDialog
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.upastithi.data.LocalPdfParser
import com.kanhaji.upastithi.data.TimeTableManager
import com.kanhaji.upastithi.features.home.domain.model.DetectedTimetable
import com.kanhaji.upastithi.features.home.domain.model.ScheduleEvent
import com.kanhaji.upastithi.features.home.domain.model.TimetableData
import com.kanhaji.upastithi.features.home.ui.HomeScreenModel
import com.kanhaji.upastithi.features.home.ui.components.AddClassDialog
import com.kanhaji.upastithi.features.home.ui.components.DaySchedulePager
import com.kanhaji.upastithi.features.home.ui.components.EditClassDialog
import com.kanhaji.upastithi.features.home.ui.components.FullScreenPdfDialog
import com.kanhaji.upastithi.features.home.ui.components.ResetTimetableDialog
import com.kanhaji.upastithi.features.home.ui.components.TimetablePdfCard
import com.kanhaji.upastithi.features.home.ui.components.TimetablePdfPreviewCard
import com.kanhaji.upastithi.features.home.ui.components.TimetableTabRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableSection(
    screenModel: HomeScreenModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var detectedList by remember { mutableStateOf<List<DetectedTimetable>>(emptyList()) }
    var showSelectDialog by remember { mutableStateOf(false) }
    var temporarySelectedPageIndex by remember { mutableStateOf<Int?>(null) }

    var timetableData by remember { mutableStateOf<TimetableData?>(TimeTableManager.activeTimetableData) }
    var originalPdfData by remember { mutableStateOf<TimetableData?>(TimeTableManager.activeTimetableData) }

    var showWarningDialog by remember { mutableStateOf(false) }
    var pendingFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var pendingFileName by remember { mutableStateOf<String?>(null) }

    var currentSelectedPageIndex by remember { mutableStateOf(0) }
    var isPdfViewExpanded by remember { mutableStateOf(false) }
    var pdfPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showFullScreenPdfDialog by remember { mutableStateOf(false) }

    var editingEvent by remember { mutableStateOf<ScheduleEvent?>(null) }
    var addingClassForDay by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var tabRowHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f && headerHeightPx > 0f) {
                    val oldOffset = headerOffsetPx
                    val newOffset = (oldOffset + delta).coerceIn(-headerHeightPx, 0f)
                    val consumed = newOffset - oldOffset
                    headerOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f && headerHeightPx > 0f) {
                    val oldOffset = headerOffsetPx
                    val newOffset = (oldOffset + delta).coerceIn(-headerHeightPx, 0f)
                    val consumed = newOffset - oldOffset
                    headerOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }
        }
    }

    val daysList = remember { listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday") }
    val initialDayIndex = remember {
        when (LocalDate.now().dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            else -> 0
        }
    }
    val pagerState = rememberPagerState(initialPage = initialDayIndex, pageCount = { daysList.size })

    LaunchedEffect(timetableData) {
        if (timetableData != null && !TimeTableManager.isCustomized) {
            TimeTableManager.setParsedTimetable(timetableData)
        }
    }

    LaunchedEffect(timetableData, currentSelectedPageIndex) {
        if (timetableData != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val localFile = File(context.filesDir, "saved_timetable.pdf")
                    if (localFile.exists()) {
                        val fileDescriptor = ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = PdfRenderer(fileDescriptor)
                        if (currentSelectedPageIndex < renderer.pageCount) {
                            val page = renderer.openPage(currentSelectedPageIndex)
                            val scale = 2.0f
                            val width = (page.width * scale).toInt()
                            val height = (page.height * scale).toInt()
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            page.close()
                            pdfPageBitmap = bitmap
                        }
                        renderer.close()
                        fileDescriptor.close()
                    }
                } catch (e: Exception) {
                    pdfPageBitmap = null
                }
            }
        } else {
            pdfPageBitmap = null
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val savedName = PrefsManager.getString("last_pdf_name")
                val savedPageIdx = PrefsManager.getInt("last_page_index")
                val localFile = File(context.filesDir, "saved_timetable.pdf")
                if (savedName != null && savedPageIdx != null && localFile.exists()) {
                    isLoading = true
                    currentSelectedPageIndex = savedPageIdx
                    val bytes = localFile.readBytes()
                    pdfBytes = bytes
                    selectedFileName = savedName
                    val parsed = LocalPdfParser.parseTimetablePage(bytes, savedPageIdx)
                    originalPdfData = parsed

                    timetableData = parsed
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    fun processTimetableBytes(bytes: ByteArray, name: String) {
        isLoading = true
        coroutineScope.launch {
            try {
                val detected = LocalPdfParser.detectTimetables(bytes)
                detectedList = detected
                isLoading = false

                if (detected.isEmpty()) {
                    errorMessage = "No timetables detected in this PDF."
                } else if (detected.size == 1) {
                    val pageIdx = detected[0].page_index
                    currentSelectedPageIndex = pageIdx
                    val parsed = LocalPdfParser.parseTimetablePage(bytes, pageIdx)
                    originalPdfData = parsed
                    timetableData = parsed
                } else {
                    temporarySelectedPageIndex = 0
                    showSelectDialog = true
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Parsing failed: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = getFileName(context, uri)
            errorMessage = null
            timetableData = null

            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                if (bytes != null) {
                    if (LocalPdfParser.isLikelyTimetable(bytes)) {
                        pdfBytes = bytes
                        selectedFileName = name
                        processTimetableBytes(bytes, name)
                    } else {
                        pendingFileBytes = bytes
                        pendingFileName = name
                        showWarningDialog = true
                    }
                } else {
                    errorMessage = "Could not read file content."
                }
            } catch (e: Exception) {
                errorMessage = "Error opening file: ${e.localizedMessage}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        val density = LocalDensity.current
        val headerOffsetDp = with(density) { (headerHeightPx + headerOffsetPx).coerceAtLeast(0f).toDp() }
        val tabRowHeightDp = with(density) { tabRowHeightPx.toDp() }

        // 1. Day Schedule Pager with full height LazyColumn per tab (scrolling behind floating tab bar)
        val activeData = TimeTableManager.activeTimetableData ?: timetableData
        activeData?.let { data ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = headerOffsetDp)
            ) {
                DaySchedulePager(
                    pagerState = pagerState,
                    daysList = daysList,
                    timetableData = data,
                    topPadding = if (tabRowHeightDp > 0.dp) tabRowHeightDp else 68.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Collapsible Top Header (PDF Card + Semester Title + PDF Preview)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, headerOffsetPx.roundToInt()) }
                .onGloballyPositioned { coordinates ->
                    headerHeightPx = coordinates.size.height.toFloat()
                }
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            TimetablePdfCard(
                selectedFileName = selectedFileName,
                onPickPdfClick = { filePickerLauncher.launch("application/pdf") },
                isCustomized = TimeTableManager.isCustomized,
                onResetClick = { showResetDialog = true }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            timetableData?.let { data ->
                Text(
                    text = data.semester,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                TimetablePdfPreviewCard(
                    isExpanded = isPdfViewExpanded,
                    onToggleExpand = { isPdfViewExpanded = !isPdfViewExpanded },
                    pdfPageBitmap = pdfPageBitmap,
                    onFullScreenClick = { showFullScreenPdfDialog = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 3. Pinned Day Tab Row
        if (timetableData != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, (headerHeightPx + headerOffsetPx).coerceAtLeast(0f).roundToInt()) }
                    .onGloballyPositioned { coordinates ->
                        tabRowHeightPx = coordinates.size.height.toFloat()
                    }
                    .padding(horizontal = 16.dp)
            ) {
                TimetableTabRow(
                    daysList = daysList,
                    selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
    }

    // Dialogs
    if (showResetDialog) {
        ResetTimetableDialog(
            onConfirmReset = {
                TimeTableManager.resetToOriginalPdf(context, originalPdfData)
                timetableData = TimeTableManager.activeTimetableData
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showSelectDialog) {
        val radioItems = detectedList.map { item ->
            RadioItem(
                label = item.name,
                onClick = { temporarySelectedPageIndex = item.page_index }
            )
        }

        RadioSelectionDialog(
            title = "Multiple Timetables Detected",
            icon = Icons.Default.MenuBook,
            options = radioItems,
            initialSelection = detectedList.indexOfFirst { it.page_index == temporarySelectedPageIndex },
            onConfirm = {
                showSelectDialog = false
                val bytes = pdfBytes
                val pageIdx = temporarySelectedPageIndex
                if (bytes != null && pageIdx != null) {
                    isLoading = true
                    currentSelectedPageIndex = pageIdx
                    coroutineScope.launch {
                        try {
                            val parsed = LocalPdfParser.parseTimetablePage(bytes, pageIdx)
                            originalPdfData = parsed
                            timetableData = parsed
                            isLoading = false

                            try {
                                val localFile = File(context.filesDir, "saved_timetable.pdf")
                                localFile.writeBytes(bytes)
                                PrefsManager.saveString("last_pdf_name", selectedFileName ?: "Timetable.pdf")
                                PrefsManager.saveInt("last_page_index", pageIdx)
                            } catch (e: Exception) {
                                Log.e("TimetableSection", "Failed to cache timetable: ${e.message}")
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Parsing page failed: ${e.localizedMessage ?: "Unknown error"}"
                        }
                    }
                }
            },
            onDismiss = { showSelectDialog = false }
        )
    }

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = {
                showWarningDialog = false
                pendingFileBytes = null
                pendingFileName = null
            },
            title = { Text("Unrecognized PDF Format") },
            text = {
                Text("This file does not appear to contain a standard grid timetable. Would you like to attempt parsing anyway?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWarningDialog = false
                        val bytes = pendingFileBytes
                        val name = pendingFileName
                        pendingFileBytes = null
                        pendingFileName = null
                        if (bytes != null && name != null) {
                            pdfBytes = bytes
                            selectedFileName = name
                            processTimetableBytes(bytes, name)
                        }
                    }
                ) {
                    Text("Proceed Anyway")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWarningDialog = false
                        pendingFileBytes = null
                        pendingFileName = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFullScreenPdfDialog && pdfPageBitmap != null) {
        FullScreenPdfDialog(
            pdfPageBitmap = pdfPageBitmap,
            fileName = selectedFileName,
            pageIndex = currentSelectedPageIndex,
            onDismiss = { showFullScreenPdfDialog = false }
        )
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "Timetable.pdf"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
    }
    return name
}
