package com.kanhaji.basics.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.screens.settings.components.colorToHex
import com.kanhaji.basics.screens.settings.components.hexToColor

@Composable
fun InitTheme() {
    val systemPrimaryColor = getSystemPrimaryColor()
    val isSystemDark = isSystemDarkTheme()

    var hasInitialized by remember { mutableStateOf(false) }

    // Update ThemeManager.isSystemDark whenever system theme changes
    LaunchedEffect(isSystemDark) {
        ThemeManager.isSystemDark = isSystemDark
        if (hasInitialized && ThemeManager.currentThemeType == ThemeManager.ThemeType.SYSTEM) {
            ThemeManager.isDarkTheme = isSystemDark
        }
    }

//    LaunchedEffect(ThemeManager.customColor.value) {
//        if (!ThemeManager.isDynamicColor) {
//            ThemeManager.customColor =
//        }
//    }

    // Initialize theme settings once when app starts
    LaunchedEffect(Unit) {
        ThemeManager.defaultSeed = systemPrimaryColor
        if (!ThemeManager.isDynamicColor)
            ThemeManager.customColor = mutableStateOf(ThemeManager.defaultSeed)
        else {
            val colorString = PrefsManager.getString(PrefsResources.CUSTOM_COLOR)
            ThemeManager.customColor = mutableStateOf(hexToColor(colorString ?: colorToHex(ThemeManager.defaultSeed)))
        }
        if (!hasInitialized) {
            // Load saved preferences
            val savedTheme = PrefsManager.getString(PrefsResources.APP_THEME)
            val savedAmoled = PrefsManager.getBoolean(PrefsResources.IS_AMOLED)
            val isDynamicColor = PrefsManager.getBoolean(PrefsResources.IS_DYNAMIC_COLOR)

            // Determine theme type (default to SYSTEM if invalid/missing)
            val themeType = when {
                savedTheme.isNullOrEmpty() -> ThemeManager.ThemeType.SYSTEM
                else -> try {
                    ThemeManager.ThemeType.valueOf(savedTheme)
                } catch (e: Exception) {
                    ThemeManager.ThemeType.SYSTEM
                }
            }

            // Apply settings to ThemeManager
            ThemeManager.currentThemeType = themeType
            ThemeManager.isAmoled = savedAmoled ?: false
            ThemeManager.isDynamicColorSupported = isDynamicColorSupported()
            ThemeManager.isDynamicColor = isDynamicColor ?: isDynamicColorSupported()

            // Set dark/light theme based on type
            ThemeManager.isDarkTheme = when (themeType) {
                ThemeManager.ThemeType.LIGHT -> false
                ThemeManager.ThemeType.DARK -> true
                ThemeManager.ThemeType.SYSTEM -> isSystemDark
            }

            hasInitialized = true
        }
    }
}
