package com.kanhaji.basics.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object SettingsScreen : Screen {
    private fun readResolve(): Any = SettingsScreen

    @Composable
    override fun Content() {
        SettingsComponent()
    }
}