package com.kanhaji.upastithi.screen.splash

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

object SplashScreen : Screen {
    private fun readResolve(): Any = SplashScreen


    @Composable
    override fun Content() {
        SplashComponent()
    }
}