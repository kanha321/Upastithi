package com.kanhaji.basics.theme

import android.os.Build

fun isDynamicColorSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
