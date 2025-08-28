package com.kanhaji.upastithi.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.screens.settings.SettingsScreen
import com.kanhaji.basics.util.Updater
import com.kanhaji.upastithi.screen.home.components.UpdateButton
import com.kanhaji.upastithi.screen.home.pages.AttendanceSection
import com.kanhaji.upastithi.screen.home.pages.CalendarSection
import com.kanhaji.upastithi.util.UpasthitiUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeComponent(
    screenModel: HomeScreenModel
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(Unit) {
        if (!UpasthitiUtils.updateChecked)
            screenModel.getUpdateInfo()
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
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = "Attendance"
                        )
                    },
                    label = { Text("Attendance") }
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> CalendarSection(screenModel)
                1 -> AttendanceSection(screenModel)
            }
        }
    }
}