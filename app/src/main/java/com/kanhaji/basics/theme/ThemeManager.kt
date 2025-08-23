package com.kanhaji.basics.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.datastore.PrefsResources
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ThemeManager {
    var defaultSeed by mutableStateOf(Color.Unspecified)
    var customColor = mutableStateOf(defaultSeed)
    var isSystemDark by mutableStateOf(false)
    var isDarkTheme by mutableStateOf(isSystemDark)
    var isAmoled by mutableStateOf(false)
    var paletteStyle by mutableStateOf(PaletteStyle.TonalSpot)
    var contrastLevel by mutableDoubleStateOf(1.0)

    var currentThemeType by mutableStateOf(ThemeType.SYSTEM)
//    var currentColorType by mutableStateOf(ColorType.SYSTEM)
    var isDynamicColor by mutableStateOf(false)
    var isDynamicColorSupported by mutableStateOf(false)

    fun setTheme(
        optionSelected: ThemeType = ThemeType.SYSTEM,
    ) {
        currentThemeType = optionSelected
        isDarkTheme = when (optionSelected) {
            ThemeType.LIGHT -> false
            ThemeType.DARK -> true
            ThemeType.SYSTEM -> isSystemDark
        }
        CoroutineScope(Dispatchers.Unconfined).launch {
            PrefsManager.saveString(PrefsResources.APP_THEME, currentThemeType.name)
        }
    }

//    fun setColorType(
//        optionSelected: ColorType = ColorType.SYSTEM,
//        customColorValue: Color? = null
//    ) {
//        currentColorType = optionSelected
//        when (optionSelected) {
//            ColorType.CUSTOM -> {
//                customColorValue?.let { customColor.value = it }
//            }
//            ColorType.SYSTEM -> {
//                // For SYSTEM type, the color will be set by InitTheme composable
//                // Don't set the color here, just update the type
//            }
//        }
//        CoroutineScope(Dispatchers.Unconfined).launch {
//            PrefsManager.saveString(PrefsResources.COLOR_TYPE, currentColorType.name)
//        }
//    }

    enum class ThemeType {
        LIGHT, DARK, SYSTEM
    }
}