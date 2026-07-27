package com.kanhaji.basics.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.basics.composables.Group
import com.kanhaji.basics.composables.GroupedLazyColumn
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.screens.settings.components.ColorPickerDialog
import com.kanhaji.basics.screens.settings.components.ThemeSelectionDialog
import com.kanhaji.basics.screens.settings.components.colorToHex
import com.kanhaji.basics.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsComponent() {
    // Make the settings list reactive to state changes
    val navigator = LocalNavigator.currentOrThrow
    val settingItems by remember {
        derivedStateOf { SettingsScreenModel.getSettingItems() }
    }
    val listState = rememberLazyListState()

    val settingsGroup = Group(
        header = "Themes",
        items = settingItems
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                actions = {

                },
                navigationIcon = {
                    if (navigator.canPop)
                        IconButton(
                            onClick = {
                                navigator.pop()
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        )
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        GroupedLazyColumn(
            groups = listOf(settingsGroup),
            keySelector = { it.id },
            contentPadding = innerPadding,
            listState = listState,
            onItemClick = { gIndex, iIndex, item ->
                item.onClick()
            },
        ) { gIndex, iIndex, item ->
            SettingsCard(item)
        }

        if (SettingsScreenModel.showThemeDialog) {
            ThemeSelectionDialog(
                onDismiss = { SettingsScreenModel.showThemeDialog = false },
                onConfirm = { SettingsScreenModel.showThemeDialog = false },
                initialSelection = when (ThemeManager.currentThemeType) {
                    ThemeManager.ThemeType.LIGHT -> 0
                    ThemeManager.ThemeType.DARK -> 1
                    ThemeManager.ThemeType.SYSTEM -> 2
                }
            )
        }

        if (SettingsScreenModel.showColorPicker) {
            ColorPickerDialog(
                customColor = ThemeManager.customColor,
                onDismiss = {
                    SettingsScreenModel.showColorPicker = false
                },
                onColorSelected = { color ->
                    ThemeManager.customColor.value = color
                    // Save the selected color to preferences
                    SettingsScreenModel.screenModelScope.launch {
                        PrefsManager.saveString(
                            PrefsResources.CUSTOM_COLOR,
                            colorToHex(color)
                        )
                    }
                }
            )
        }
    }
}
