package com.kanhaji.upastithi.screen.splash

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.kanhaji.upastithi.screen.home.HomeScreen

object SplashScreen : Screen {
    private fun readResolve(): Any = SplashScreen


    @Composable
    override fun Content() {
        SplashComponent()
    }
}