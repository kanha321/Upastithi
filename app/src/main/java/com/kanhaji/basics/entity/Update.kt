package com.kanhaji.basics.entity

import kotlinx.serialization.Serializable

@Serializable
data class Update(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val downloadMCA1: String,
    val downloadMCA3: String,
    val changelog: String
)
