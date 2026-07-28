package com.kanhaji.upasthiti.features.splash.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.upasthiti.features.home.ui.HomeScreen
import com.kanhaji.upasthiti.util.UpasthitiUtils
import kotlinx.coroutines.delay

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.ui.platform.LocalContext
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.upasthiti.data.LocalPdfParser
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.data.repository.TimetableRepositoryImpl
import com.kanhaji.upasthiti.util.KToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SplashComponent() {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var startAnimation by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var isProcessingFile by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingFile = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val name = getFileName(context, uri)
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val contentStr = try { String(bytes, Charsets.UTF_8).trim() } catch (_: Exception) { "" }

                        if (name.endsWith(".upasthiti", ignoreCase = true) || name.endsWith(".json", ignoreCase = true) || (contentStr.startsWith("{") && contentStr.endsWith("}"))) {
                            val imported = TimeTableManager.importTimetableJson(contentStr, context)
                            val repository = TimetableRepositoryImpl()
                            repository.setParsedTimetable(imported, name, 0)
                            PrefsManager.saveString("last_pdf_name", name)
                            withContext(Dispatchers.Main) {
                                KToast.show(context, "Timetable imported successfully!")
                                navigator.replace(HomeScreen)
                            }
                        } else if (LocalPdfParser.isLikelyTimetable(bytes)) {
                            val detected = LocalPdfParser.detectTimetables(bytes)
                            val pageIdx = if (detected.isNotEmpty()) detected[0].page_index else 0
                            val parsed = LocalPdfParser.parseTimetablePage(bytes, pageIdx)

                            val localFile = File(context.filesDir, "saved_timetable.pdf")
                            localFile.writeBytes(bytes)

                            val repository = TimetableRepositoryImpl()
                            repository.setParsedTimetable(parsed, name, pageIdx)
                            PrefsManager.saveString("last_pdf_name", name)
                            PrefsManager.saveInt("last_page_index", pageIdx)

                            withContext(Dispatchers.Main) {
                                KToast.show(context, "PDF Timetable parsed successfully!")
                                navigator.replace(HomeScreen)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                isProcessingFile = false
                                KToast.show(context, "Invalid file format. Select a valid PDF or .upasthiti file.")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            isProcessingFile = false
                            KToast.show(context, "Unable to read file content.")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessingFile = false
                        KToast.show(context, "Import error: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    val logoOffsetAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else -100f,
        animationSpec = tween(
            durationMillis = 800,
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        ),
        label = "logoOffset"
    )

    val logoScaleAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(
            durationMillis = 800,
            easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        ),
        label = "logoScale"
    )

    val logoAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 100,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "logoAlpha"
    )

    val titleOffsetAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 50f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = 200,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "titleOffset"
    )

    val titleAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = 300,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "titleAlpha"
    )

    val subtitleAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            delayMillis = 500,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "subtitleAlpha"
    )

    val versionAlphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = 700,
            easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        ),
        label = "versionAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)

        val hasStoredPdf = File(context.filesDir, "saved_timetable.pdf").exists() && PrefsManager.getString("last_pdf_name") != null
        val hasStoredJson = TimeTableManager.loadOriginalTimetableJson(context) != null || TimeTableManager.loadModifiedTimetableJson(context) != null
        val hasActiveData = TimeTableManager.activeTimetableData != null

        if (hasActiveData || hasStoredPdf || hasStoredJson) {
            if (!hasActiveData) {
                try {
                    val repository = TimetableRepositoryImpl()
                    val modifiedJson = TimeTableManager.loadModifiedTimetableJson(context)
                    if (modifiedJson != null) {
                        val stamped = TimeTableManager.stampTimetableId(modifiedJson)
                        repository.setParsedTimetable(stamped, "modified_timetable.json", stamped.pageIndex)
                    } else {
                        val originalJson = TimeTableManager.loadOriginalTimetableJson(context)
                        if (originalJson != null) {
                            val stamped = TimeTableManager.stampTimetableId(originalJson)
                            repository.setParsedTimetable(stamped, "original_timetable.json", stamped.pageIndex)
                        } else if (hasStoredPdf) {
                            val savedName = PrefsManager.getString("last_pdf_name") ?: "saved_timetable.pdf"
                            val savedPageIdx = PrefsManager.getInt("last_page_index") ?: 0
                            val bytes = File(context.filesDir, "saved_timetable.pdf").readBytes()
                            val parsed = LocalPdfParser.parseTimetablePage(bytes, savedPageIdx)
                            repository.setParsedTimetable(parsed, savedName, savedPageIdx)
                        }
                    }
                } catch (_: Exception) {}
            }
            navigator.replace(HomeScreen)
        } else {
            showUploadDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(88.dp)
                    .scale(logoScaleAnimation)
                    .alpha(logoAlphaAnimation)
                    .graphicsLayer {
                        translationY = logoOffsetAnimation
                    },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChecklistRtl,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Upasthiti",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .alpha(titleAlphaAnimation)
                    .graphicsLayer {
                        translationY = titleOffsetAnimation
                    }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "For MNNIT Students",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(subtitleAlphaAnimation)
            )
        }

        Text(
            text = "Version " + UpasthitiUtils.appVersionName?.replace("v", ""),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(versionAlphaAnimation)
        )

        if (showUploadDialog) {
            AlertDialog(
                onDismissRequest = { },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                icon = {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Upload Timetable",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Upasthiti requires a timetable to show your daily classes and track attendance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Please upload an official MNNIT MCA timetable PDF or an exported .upasthiti file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        if (isProcessingFile) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text("Parsing timetable file...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        enabled = !isProcessingFile,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Select File (.pdf / .upasthiti)")
                    }
                }
            )
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "timetable.pdf"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}
