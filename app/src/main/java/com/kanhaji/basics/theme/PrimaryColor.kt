package com.kanhaji.basics.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun getSystemPrimaryColor(): Color {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val colorScheme = dynamicLightColorScheme(context)
        colorScheme.primary
    } else {
        Color(ContextCompat.getColor(context, android.R.color.holo_blue_light))
    }
}
