package com.kanhaji.basics.entity

import kotlinx.serialization.Serializable

@Serializable
enum class UpdatePriority {
    OPTIONAL,
    RECOMMENDED,
    CRITICAL
}

@Serializable
data class Update(
    val latestVersionCode: Long = 0,
    val latestVersionName: String = "",
    val downloadUrl: String = "",
    val changelog: String = "https://kanha321.github.io/Upastithi/Changelog.md",
    val sha256: String = "",
    val fileSizeBytes: Long = 0,
    val forceUpdate: Boolean = false,
    val priority: String = "OPTIONAL",
    val minSupportedVersionCode: Int = 0,
    // Legacy fields kept for backward compatibility with older serializer caches
    val downloadMCA1: String = "",
    val downloadMCA3: String = ""
) {
    fun getEffectivePriority(currentVersionCode: Long): UpdatePriority {
        return when {
            priority.equals("CRITICAL", ignoreCase = true) -> UpdatePriority.CRITICAL
            forceUpdate -> UpdatePriority.CRITICAL
            minSupportedVersionCode > 0 && currentVersionCode < minSupportedVersionCode -> UpdatePriority.CRITICAL
            priority.equals("RECOMMENDED", ignoreCase = true) -> UpdatePriority.RECOMMENDED
            else -> UpdatePriority.OPTIONAL
        }
    }
}
