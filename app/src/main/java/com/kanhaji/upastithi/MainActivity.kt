package com.kanhaji.upastithi

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.kanhaji.basics.datastore.PrefsManager
import com.kanhaji.basics.theme.BasicKolorTheme
import com.kanhaji.upastithi.screen.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PrefsManager.init(this)
        AndroidContext.appContext = this@MainActivity
        setContent {
            BasicKolorTheme {
                Navigator(SplashScreen) { navigator ->
                    SlideTransition(navigator)
                }
            }
        }
    }
}

object AndroidContext {
    lateinit var appContext: Context
}