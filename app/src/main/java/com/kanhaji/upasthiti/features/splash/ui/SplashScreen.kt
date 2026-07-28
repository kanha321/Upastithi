package com.kanhaji.upasthiti.features.splash.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object SplashScreen : Screen {
    private fun readResolve(): Any = SplashScreen

    @Composable
    override fun Content() {
        SplashComponent()
    }
}
