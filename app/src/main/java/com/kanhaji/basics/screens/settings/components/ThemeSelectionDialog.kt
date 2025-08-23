package com.kanhaji.basics.screens.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kanhaji.basics.composables.RadioItem
import com.kanhaji.basics.composables.RadioSelectionDialog
import com.kanhaji.basics.theme.ThemeManager

@Composable
fun ThemeSelectionDialog(
    initialSelection: Int? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Save the original theme when dialog opens
    val originalThemeType = remember { ThemeManager.currentThemeType }
    val originalIsDarkTheme = remember { ThemeManager.isDarkTheme }

    RadioSelectionDialog(
        title = "Select App Theme",
        initialSelection = initialSelection,
        options = listOf(
            RadioItem(
                label = "Light",
                onClick = {
                    ThemeManager.setTheme(ThemeManager.ThemeType.LIGHT)
                }
            ),
            RadioItem(
                label = "Dark",
                onClick = {
                    ThemeManager.setTheme(ThemeManager.ThemeType.DARK)
                }
            ),
            RadioItem(
                label = "System",
                onClick = {
                    ThemeManager.setTheme(ThemeManager.ThemeType.SYSTEM)
                }
            )
        ),
        icon = Icons.Outlined.Add,
        onConfirm = onConfirm,
        onDismiss = {
            // Restore original theme on cancel
            ThemeManager.currentThemeType = originalThemeType
            ThemeManager.isDarkTheme = originalIsDarkTheme
            onDismiss()
        }
    )
}