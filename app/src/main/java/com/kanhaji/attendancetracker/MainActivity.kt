package com.kanhaji.attendancetracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kanhaji.attendancetracker.composable.KRadioSelector
import com.kanhaji.attendancetracker.screen.home.HomeScreen
import com.kanhaji.attendancetracker.ui.theme.AttendanceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PrefsManager.init(this@MainActivity)
        AndroidContext.appContext = this@MainActivity
        setContent {
            AttendanceTrackerTheme(
                darkTheme = false,
            ) {
                // Get the system UI controller
                val systemUiController = rememberSystemUiController()
                // Get the primary color from the theme
                val primaryColor = MaterialTheme.colorScheme.primary

                // Use a SideEffect to update the system bars
                SideEffect {
                    // Set the status bar color
                    systemUiController.setStatusBarColor(
                        color = primaryColor,
                        darkIcons = true // Set to false if you want light icons
                    )

                    // You can also set the navigation bar color if you want
                    // systemUiController.setNavigationBarColor(
                    //     color = primaryColor,
                    //     darkIcons = false
                    // )
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Navigator(HomeScreen) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                }
            }
        }
    }
}

object AndroidContext {
    lateinit var appContext: Context
}

//@Composable
//fun ExampleScreen() {
//    val items = listOf("Option 1", "Option 2", "Option 3")
//
//    KRadioSelector(
//        items = items,
//        initialSelection = items.first(),
//        onSelectionChanged = { selected ->
//            // Handle selection change
//        }
//    )
//}
