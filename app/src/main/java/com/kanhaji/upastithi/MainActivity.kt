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
import com.kanhaji.upastithi.util.UpasthitiUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PrefsManager.init(this)
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(applicationContext)
        AndroidContext.appContext = this@MainActivity
        UpasthitiUtils.appVersionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
        UpasthitiUtils.appVersionName = packageManager.getPackageInfo(packageName, 0).versionName
        println("App Version Code: ${UpasthitiUtils.appVersionCode}")
        println("App Version Name: ${UpasthitiUtils.appVersionName}")
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