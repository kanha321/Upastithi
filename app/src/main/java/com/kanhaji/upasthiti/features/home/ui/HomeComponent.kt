package com.kanhaji.upasthiti.features.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.screens.settings.SettingsScreen
import com.kanhaji.basics.util.Updater
import com.kanhaji.upasthiti.data.LocalPdfParser
import com.kanhaji.upasthiti.data.TimeTableManager
import com.kanhaji.upasthiti.features.home.ui.components.FloatingSpringBottomBar
import com.kanhaji.upasthiti.features.home.ui.components.UpdateButton
import com.kanhaji.upasthiti.features.home.ui.pages.AttendanceSection
import com.kanhaji.upasthiti.features.home.ui.pages.CalendarSection
import com.kanhaji.upasthiti.features.home.ui.pages.TimetableSection
import com.kanhaji.upasthiti.util.UpasthitiUtils
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kanhaji.upasthiti.features.home.ui.components.ShareExportBottomSheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponent(
    screenModel: HomeScreenModel
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.currentOrThrow
    val context = androidx.compose.ui.platform.LocalContext.current
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var showUninstallDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!UpasthitiUtils.updateChecked)
            screenModel.getUpdateInfo()
    }

    LaunchedEffect(Unit) {
        if (!UpasthitiUtils.hasCheckedUninstallThisSession) {
            UpasthitiUtils.hasCheckedUninstallThisSession = true
            delay(1000)
            try {
                context.packageManager.getPackageInfo("com.kanhaji.upastithi", 0)
                showUninstallDialog = true
            } catch (_: Exception) { }
        }
    }

    // Early PDF auto-load: parse cached timetable at app launch so all tabs see dynamic subjects
    LaunchedEffect(Unit) {
        if (TimeTableManager.activeTimetableData != null) return@LaunchedEffect
        try {
            val savedName = PrefsManager.getString("last_pdf_name")
            val savedPageIdx = PrefsManager.getInt("last_page_index")
            val localFile = File(context.filesDir, "saved_timetable.pdf")
            if (savedName != null && savedPageIdx != null && localFile.exists()) {
                val bytes = localFile.readBytes()
                val parsed = LocalPdfParser.parseTimetablePage(bytes, savedPageIdx)
                val repository = com.kanhaji.upasthiti.features.home.data.repository.TimetableRepositoryImpl()
                repository.setParsedTimetable(parsed, savedName, savedPageIdx)
            }
        } catch (_: Exception) { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upasthiti") },
                actions = {
                    IconButton(onClick = {
                        navigator.push(SettingsScreen)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                    IconButton(onClick = {
                        showShareBottomSheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share & Export"
                        )
                    }
                    AnimatedVisibility(screenModel.isUpdateAvailable && Updater.downloadProgress != 1f) {
                        UpdateButton()
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showShareBottomSheet) {
            ShareExportBottomSheet(
                onDismiss = { showShareBottomSheet = false }
            )
        }

        if (showUninstallDialog) {
            AlertDialog(
                onDismissRequest = { showUninstallDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Uninstall Old App Version",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "We detected that you still have the legacy version of Upasthiti (com.kanhaji.upastithi) installed on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Please uninstall the old version to save storage space and avoid duplicate app confusion.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showUninstallDialog = false
                            val targetPackage = "com.kanhaji.upastithi"
                            val packageUri = "package:$targetPackage".toUri()
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DELETE, packageUri)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_UNINSTALL_PACKAGE, packageUri).apply {
                                        putExtra(android.content.Intent.EXTRA_RETURN_RESULT, true)
                                    }
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    com.kanhaji.upasthiti.util.KToast.show(context, "Could not launch uninstall: ${ex.localizedMessage}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Uninstall Old App")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUninstallDialog = false }) {
                        Text("Later")
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> CalendarSection(screenModel)
                    1 -> AttendanceSection(screenModel)
                    2 -> TimetableSection(screenModel)
                }
            }

            // Floating Capsule Overlay (Exact KernelSU-Next Architecture)
            FloatingSpringBottomBar(
                selectedPage = pagerState.currentPage,
                onPageSelected = { page ->
                    scope.launch { pagerState.animateScrollToPage(page) }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
