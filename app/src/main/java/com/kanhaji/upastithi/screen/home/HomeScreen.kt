package com.kanhaji.upastithi.screen.home

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

object HomeScreen: Screen {
    private fun readResolve(): Any = HomeScreen

    @Composable
    override fun Content() {

        val screenModel = rememberScreenModel {
            HomeScreenModel()
        }

        HomeComponent(screenModel)
    }
}