package com.kanhaji.upastithi.features.home.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.kanhaji.upastithi.data.TimeTableManager
import com.kanhaji.upastithi.features.home.data.AttendanceStatus
import com.kanhaji.upastithi.features.home.data.AttendanceStorage
import com.kanhaji.upastithi.features.home.domain.model.TimetableData
import com.kanhaji.upastithi.util.KToast
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AttendanceExportRecord(
    val dateIso: String,
    val time: String,
    val courseCode: String,
    val courseName: String,
    val status: String
)

@Serializable
data class AttendanceExportData(
    val version: Int = 1,
    val exportDate: String,
    val records: List<AttendanceExportRecord>
)

private val exportJson = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareExportBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTimetableIndex by remember { mutableIntStateOf(0) } // 0 = Original, 1 = Modified

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Share & Export Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Option 1: Attendance Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Attendance Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Export all marked attendance history (.upasthiti)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportAttendanceData(context, share = false)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save")
                        }

                        Button(
                            onClick = {
                                exportAttendanceData(context, share = true)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }

            // Option 2: Timetable Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                text = "Timetable Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Export schedule & subjects (.upasthiti)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val isTimetableModified = remember {
                        TimeTableManager.isCustomized || TimeTableManager.loadModifiedTimetableJson(context) != null
                    }

                    if (isTimetableModified) {
                        // Selector for Original vs Modified
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = selectedTimetableIndex == 0,
                                onClick = { selectedTimetableIndex = 0 },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("Original")
                            }
                            SegmentedButton(
                                selected = selectedTimetableIndex == 1,
                                onClick = { selectedTimetableIndex = 1 },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("Modified")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportTimetableData(context, isModified = isTimetableModified && selectedTimetableIndex == 1, share = false)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save")
                        }

                        Button(
                            onClick = {
                                exportTimetableData(context, isModified = isTimetableModified && selectedTimetableIndex == 1, share = true)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            }

            // Option 3: Share App
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Column {
                            Text(
                                text = "Share Upasthiti App",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Share app link with friends & classmates",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { shareApp(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun sanitizeFileName(name: String): String {
    return name.trim()
        .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')
}

private fun exportAttendanceData(context: Context, share: Boolean) {
    try {
        val list = AttendanceStorage.loadAttendanceList(context)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val records = list.map {
            AttendanceExportRecord(
                dateIso = it.date.toString(),
                time = it.time,
                courseCode = it.subject.subjectId,
                courseName = it.subject.displayName,
                status = it.attendanceStatus?.name ?: ""
            )
        }
        val exportData = AttendanceExportData(exportDate = today, records = records)
        val jsonStr = exportJson.encodeToString(exportData)

        val activeData = TimeTableManager.activeTimetableData
        val semesterTag = activeData?.semester?.ifBlank { null }
        val baseTitle = if (!semesterTag.isNullOrBlank()) sanitizeFileName(semesterTag) else "Upasthiti"
        val fileName = "${baseTitle}_Attendance_${today}.upasthiti"

        if (share) {
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonStr)
            shareFileViaIntent(context, file, "Share Attendance Data (.upasthiti)")
        } else {
            saveToDownloadsOrInternal(context, fileName, jsonStr)
        }
    } catch (e: Exception) {
        KToast.show(context, "Export failed: ${e.localizedMessage}")
    }
}

private fun exportTimetableData(context: Context, isModified: Boolean, share: Boolean) {
    try {
        val data: TimetableData? = if (isModified) {
            TimeTableManager.loadModifiedTimetableJson(context) ?: TimeTableManager.activeTimetableData
        } else {
            TimeTableManager.loadOriginalTimetableJson(context) ?: TimeTableManager.activeTimetableData
        }

        if (data == null) {
            KToast.show(context, "No timetable data available to export.")
            return
        }

        val semesterTag = data.semester.ifBlank { TimeTableManager.activeTimetableData?.semester }?.ifBlank { null }
        val baseTitle = if (!semesterTag.isNullOrBlank()) sanitizeFileName(semesterTag) else "Upasthiti_Timetable"
        val typeTag = if (isModified) "Modified" else "Original"
        val fileName = "${baseTitle}_${typeTag}.upasthiti"
        val jsonStr = exportJson.encodeToString(data)

        if (share) {
            val file = File(context.cacheDir, fileName)
            file.writeText(jsonStr)
            shareFileViaIntent(context, file, "Share Timetable (.upasthiti)")
        } else {
            saveToDownloadsOrInternal(context, fileName, jsonStr)
        }
    } catch (e: Exception) {
        KToast.show(context, "Export failed: ${e.localizedMessage}")
    }
}

private fun shareFileViaIntent(context: Context, file: File, title: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    } catch (e: Exception) {
        KToast.show(context, "Sharing error: ${e.localizedMessage}")
    }
}

private fun saveToDownloadsOrInternal(context: Context, fileName: String, content: String) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val destFile = File(downloadsDir, fileName)
        destFile.writeText(content)
        KToast.show(context, "Saved to Downloads: ${destFile.name}")
    } catch (e: Exception) {
        try {
            val fallbackFile = File(context.filesDir, fileName)
            fallbackFile.writeText(content)
            KToast.show(context, "Saved to App Files: ${fallbackFile.name}")
        } catch (ex: Exception) {
            KToast.show(context, "Save failed: ${ex.localizedMessage}")
        }
    }
}

private fun shareApp(context: Context) {
    try {
        val shareText = "Manage your timetable and track class attendance easily with Upasthiti!\nDownload now: https://github.com/kanhaji/Upastithi"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Share Upasthiti App"))
    } catch (e: Exception) {
        KToast.show(context, "Share app error: ${e.localizedMessage}")
    }
}
