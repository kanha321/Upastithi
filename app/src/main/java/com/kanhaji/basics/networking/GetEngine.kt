package com.kanhaji.basics.networking

import io.ktor.client.engine.okhttp.OkHttp

fun getEngine() = OkHttp.create()