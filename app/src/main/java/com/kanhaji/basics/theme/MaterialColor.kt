package com.kanhaji.basics.theme

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.kanhaji.basics.screens.settings.components.hexToColor
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

@Composable
fun BasicKolorTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemPrimaryColor = getSystemPrimaryColor()
    val isSystemDark = isSystemDarkTheme()

    // Step 1: Observe system dark mode changes
    ObserveSystemDarkMode(isSystemDark)

    // Step 2: Initialize theme from preferences (remembered across config changes)
    InitializeThemeFromPreferences(systemPrimaryColor, isSystemDark)

    // Step 3: Resolve seed color
    val seedColor = rememberSeedColor(context)

    // Step 4: Create color scheme
    val colorScheme = rememberDynamicColorScheme(
        primary = seedColor,
        isDark = ThemeManager.isDarkTheme,
        isAmoled = ThemeManager.isAmoled,
        contrastLevel = ThemeManager.contrastLevel,
        style = ThemeManager.paletteStyle
    )

    // Step 5: Apply system UI styling
    ApplySystemUiColors(colorScheme)

    // Step 6: Apply MaterialTheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
private fun ObserveSystemDarkMode(isSystemDark: Boolean) {
    var hasInitialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isSystemDark) {
        ThemeManager.isSystemDark = isSystemDark
        if (hasInitialized && ThemeManager.currentThemeType == ThemeManager.ThemeType.SYSTEM) {
            ThemeManager.isDarkTheme = isSystemDark
        }
        hasInitialized = true
    }
}

@Composable
private fun InitializeThemeFromPreferences(systemPrimaryColor: Color, isSystemDark: Boolean) {
    var hasInitialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasInitialized) {
            ThemeManager.defaultSeed = systemPrimaryColor
            ThemeManager.customColor.value = systemPrimaryColor

            val savedTheme = PrefsManager.getString(PrefsResources.APP_THEME)
            val savedAmoled = PrefsManager.getBoolean(PrefsResources.IS_AMOLED)
            val savedDynamic = PrefsManager.getBoolean(PrefsResources.IS_DYNAMIC_COLOR)
            val savedCustomColor = PrefsManager.getString(PrefsResources.CUSTOM_COLOR)
            val savedPaletteStyle = PrefsManager.getString(PrefsResources.PALETTE_STYLE)
            val savedContrastLevel = PrefsManager.getDouble(PrefsResources.CONTRAST_LEVEL)

            ThemeManager.currentThemeType = savedTheme?.let {
                runCatching { ThemeManager.ThemeType.valueOf(it) }
                    .getOrDefault(ThemeManager.ThemeType.SYSTEM)
            } ?: ThemeManager.ThemeType.SYSTEM

            ThemeManager.isAmoled = savedAmoled ?: false
            ThemeManager.isDynamicColorSupported = isDynamicColorSupported()
            ThemeManager.isDynamicColor = savedDynamic ?: ThemeManager.isDynamicColorSupported

            ThemeManager.customColor = mutableStateOf(
                savedCustomColor?.let { hexToColor(it) } ?: systemPrimaryColor
            )

            ThemeManager.paletteStyle = savedPaletteStyle?.let {
                runCatching { PaletteStyle.valueOf(it) }.getOrNull()
            } ?: ThemeManager.paletteStyle

            ThemeManager.contrastLevel = savedContrastLevel ?: ThemeManager.contrastLevel

            ThemeManager.isDarkTheme = when (ThemeManager.currentThemeType) {
                ThemeManager.ThemeType.LIGHT -> false
                ThemeManager.ThemeType.DARK -> true
                ThemeManager.ThemeType.SYSTEM -> isSystemDark
            }

            hasInitialized = true
        }
    }
}

@Composable
private fun rememberSeedColor(context: Context): Color {
    return if (ThemeManager.isDynamicColor) {
        try {
            val wm = WallpaperManager.getInstance(context)
            val sysColor = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
            Color(sysColor?.primaryColor?.toArgb() ?: ThemeManager.customColor.value.toArgb())
        } catch (_: Exception) {
            ThemeManager.customColor.value
        }
    } else {
        ThemeManager.customColor.value
    }
}

@Composable
private fun ApplySystemUiColors(colorScheme: ColorScheme) {
    val view = LocalView.current
    val activity = view.context as? Activity ?: return

    SideEffect {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = colorScheme.background.toArgb()

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !ThemeManager.isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !ThemeManager.isDarkTheme
    }
}