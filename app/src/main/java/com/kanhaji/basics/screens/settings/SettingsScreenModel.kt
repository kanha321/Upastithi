package com.kanhaji.basics.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.composables.KButton
import com.kanhaji.basics.composables.KSwitch
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.entity.SettingItems
import com.kanhaji.basics.extensions.toTitleCase
import com.kanhaji.basics.icons.Kmp
import com.kanhaji.basics.screens.settings.components.hexToColor
import com.kanhaji.basics.theme.ThemeManager
import com.kanhaji.basics.theme.isDynamicColorSupported
import kotlinx.coroutines.launch

object SettingsScreenModel : ScreenModel {

    var showColorPicker by mutableStateOf(false)
    var showThemeDialog by mutableStateOf(false)

    private fun updateAmoledSetting(enabled: Boolean) {
        ThemeManager.isAmoled = enabled
        screenModelScope.launch {
            PrefsManager.saveBoolean(
                PrefsResources.IS_AMOLED,
                ThemeManager.isAmoled
            )
        }
    }

    private fun updateDynamicColorSetting(enabled: Boolean) {
        ThemeManager.isDynamicColor = enabled
        screenModelScope.launch {
            PrefsManager.saveBoolean(
                PrefsResources.IS_DYNAMIC_COLOR,
                ThemeManager.isDynamicColor
            )
        }
    }

    fun getSettingItems(): List<SettingItems> {
        val items = mutableListOf<SettingItems>()

        // App Theme
        items.add(
            SettingItems(
                id = "app_theme",
                title = "App Theme",
                description = "Select the theme for app",
                icon = if (ThemeManager.isDarkTheme) Icons.Outlined.Brightness4 else Icons.Outlined.Brightness5,
                widget = {
                    KButton(
                        onClick = { showThemeDialog = true },
                        contentPadding = PaddingValues(horizontal = 18.dp)
                    ) {
                        BasicText(
                            text = ThemeManager.currentThemeType.name.toTitleCase(),
                            autoSize = TextAutoSize.StepBased(
                                maxFontSize = 14.sp,
                                minFontSize = 6.sp
                            ),
                            maxLines = 1,
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                },
                onClick = { showThemeDialog = true }
            )
        )

        // Pitch Black
        if (ThemeManager.isDarkTheme) {
            items.add(
                SettingItems(
                    id = "pitch_black",
                    title = "Pitch Black",
                    description = "Useful for OLED displays to save battery",
                    icon = Icons.Outlined.DarkMode,
                    widget = {
                        KSwitch(
                            state = ThemeManager.isAmoled,
                            onCheckedChange = { updateAmoledSetting(it) }
                        )
                    },
                    onClick = { updateAmoledSetting(!ThemeManager.isAmoled) }
                )
            )
        }

        // Dynamic Color
        if (isDynamicColorSupported()) {
            items.add(
                SettingItems(
                    id = "dynamic_color",
                    title = "Dynamic Color",
                    description = "Enable wallpaper based colors",
                    icon = Icons.Outlined.ColorLens,
                    widget = {
                        KSwitch(
                            state = ThemeManager.isDynamicColor,
                            onCheckedChange = { updateDynamicColorSetting(it) }
                        )
                    },
                    onClick = { updateDynamicColorSetting(!ThemeManager.isDynamicColor) }
                )
            )
        }

        // App Color
        if (!ThemeManager.isDynamicColor) {
            items.add(
                SettingItems(
                    id = "app_color",
                    title = "App Color",
                    description = "Select a color for your app",
                    icon = Icons.Outlined.Colorize,
                    widget = {},
                    onClick = { showColorPicker = true }
                )
            )
        }
        return items
    }
}