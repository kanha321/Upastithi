package com.kanhaji.basics.networking

import kotlinx.serialization.json.Json

val httpClient by lazy {
    buildHttpClient(getEngine())
}

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}