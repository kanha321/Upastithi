package com.kanhaji.basics.entity

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class SettingItems (
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val widget: @Composable () -> Unit = {},
)

//interface SettingsItem{
//    val title: String
//    val description: String
//    val icon: ImageVector
//    @Composable fun widget()
//}
