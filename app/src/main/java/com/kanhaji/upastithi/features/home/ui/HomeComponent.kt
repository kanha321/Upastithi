package com.kanhaji.upastithi.features.home.ui

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
import com.kanhaji.upastithi.data.LocalPdfParser
import com.kanhaji.upastithi.data.TimeTableManager
import com.kanhaji.upastithi.features.home.ui.components.FloatingSpringBottomBar
import com.kanhaji.upastithi.features.home.ui.components.UpdateButton
import com.kanhaji.upastithi.features.home.ui.pages.AttendanceSection
import com.kanhaji.upastithi.features.home.ui.pages.CalendarSection
import com.kanhaji.upastithi.features.home.ui.pages.TimetableSection
import com.kanhaji.upastithi.util.UpasthitiUtils
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponent(
    screenModel: HomeScreenModel
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.currentOrThrow
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        if (!UpasthitiUtils.updateChecked)
            screenModel.getUpdateInfo()
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
                val repository = com.kanhaji.upastithi.features.home.data.repository.TimetableRepositoryImpl()
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
                    AnimatedVisibility(screenModel.isUpdateAvailable && Updater.downloadProgress != 1f) {
                        UpdateButton()
                    }
                }
            )
        }
    ) { innerPadding ->
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
