package com.kanhaji.basics.screens.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.Brightness5
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.kanhaji.basics.composables.KButton
import com.kanhaji.basics.composables.KSwitch
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.entity.SettingItems
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.extensions.toTitleCase
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.basics.theme.ThemeManager
import com.kanhaji.basics.theme.isDynamicColorSupported
import com.kanhaji.upasthiti.util.KToast
import com.kanhaji.upasthiti.util.UpasthitiUtils
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch

object SettingsScreenModel : ScreenModel {

    var showColorPicker by mutableStateOf(false)
    var showThemeDialog by mutableStateOf(false)
    var showSaturdayBottomSheet by mutableStateOf(false)

    fun getAcademicItems(): List<SettingItems> {
        val items = mutableListOf<SettingItems>()
        val isSatEnabled = com.kanhaji.upasthiti.features.home.data.SaturdayScheduleManager.isEnabled
        val satMode = com.kanhaji.upasthiti.features.home.data.SaturdayScheduleManager.mode
        val desc = if (!isSatEnabled) "Disabled (Saturdays treated as weekend)"
                   else if (satMode == com.kanhaji.upasthiti.features.home.data.SaturdayMode.AUTO) "Active • Auto Rotation (Mon ➔ Fri)"
                   else "Active • Fixed Weekday"

        items.add(
            SettingItems(
                id = "saturday_classes",
                title = "Saturday Schedule",
                description = desc,
                icon = Icons.Outlined.CalendarMonth,
                onClick = { showSaturdayBottomSheet = true }
            )
        )
        return items
    }

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

    var isCheckingUpdate by mutableStateOf(false)

    fun checkUpdateManual(context: Context) {
        if (isCheckingUpdate) return
        isCheckingUpdate = true
        KToast.show(context, "Checking for updates...")

        screenModelScope.launch {
            try {
                val response: HttpResponse = httpClient.get(
                    UpasthitiUtils.BASE_URL + UpasthitiUtils.UPDATE_ENDPOINT
                )
                val updateData: Update = response.body()
                
                if (updateData.latestVersionCode > UpasthitiUtils.appVersionCode) {
                    KToast.show(
                        context,
                        "🎉 New update available: ${updateData.latestVersionName}! Opening releases page...",
                        Toast.LENGTH_LONG
                    )
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/kanha321/Upastithi/releases".toUri()
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                } else if (updateData.latestVersionCode > 0) {
                    KToast.show(
                        context,
                        "You are on the latest version (${UpasthitiUtils.appVersionName ?: "v3.0.0"})"
                    )
                } else {
                    KToast.show(
                        context,
                        "Unable to check for updates. Check your internet connection."
                    )
                }
            } catch (e: Exception) {
                KToast.show(
                    context,
                    "Unable to check for updates: ${e.localizedMessage ?: "Network error"}"
                )
            } finally {
                isCheckingUpdate = false
            }
        }
    }

    fun getAboutItems(context: Context): List<SettingItems> {
        val items = mutableListOf<SettingItems>()

        // Check for Updates
        items.add(
            SettingItems(
                id = "check_updates",
                title = "Check for Updates",
                description = "Current version: ${UpasthitiUtils.appVersionName ?: "v3.0.0"} (Code ${UpasthitiUtils.appVersionCode})",
                icon = Icons.Outlined.SystemUpdate,
                widget = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Check for Updates",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                onClick = {
                    checkUpdateManual(context)
                }
            )
        )

        // Developer Profile
        items.add(
            SettingItems(
                id = "developer_github",
                title = "Developer Profile",
                description = "Visit Kanha's GitHub profile",
                icon = Icons.Outlined.Person,
                widget = {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = "Open Link",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/kanha321".toUri()
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                }
            )
        )

        // Upasthiti Repository
        items.add(
            SettingItems(
                id = "upasthiti_repo",
                title = "Upasthiti Repository",
                description = "View source code & releases on GitHub",
                icon = Icons.Outlined.Code,
                widget = {
                    Icon(
                        imageVector = Icons.Outlined.OpenInNew,
                        contentDescription = "Open Link",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/kanha321/Upastithi".toUri()
                        )
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                }
            )
        )

        return items
    }
}
