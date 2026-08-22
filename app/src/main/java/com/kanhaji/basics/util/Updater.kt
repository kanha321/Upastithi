package com.kanhaji.basics.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.networking.httpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed interface UpdateDownloadState {
    object Idle : UpdateDownloadState
    object Connecting : UpdateDownloadState
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : UpdateDownloadState
    object Verifying : UpdateDownloadState
    data class ReadyToInstall(val apkFile: File) : UpdateDownloadState
    data class Error(val message: String, val canRetry: Boolean = true) : UpdateDownloadState
}

object Updater {

    var downloadState by mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle)
    var downloadProgress by mutableFloatStateOf(0f)
    var bytesDownloaded by mutableLongStateOf(0L)
    var totalBytes by mutableLongStateOf(0L)

    var changelogText by mutableStateOf<String?>(null)
    var isChangelogLoading by mutableStateOf(false)

    var update by mutableStateOf<Update?>(null)
    var isForceUpdate by mutableStateOf(false)
    var updatePriority by mutableStateOf(com.kanhaji.basics.entity.UpdatePriority.OPTIONAL)
    var showUpdateBottomSheet by mutableStateOf(false)
    var showCriticalDialog by mutableStateOf(false)
    var hasAutoPromptedUpdateThisSession = false

    private var downloadJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // Pre-warmed OkHttpClient with connection pooling for instant handshake
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun prewarmConnection(url: String) {
        if (url.isBlank()) return
        scope.launch {
            try {
                // Pre-resolve DNS and warm SSL session
                val request = Request.Builder().url(url).head().build()
                okHttpClient.newCall(request).execute().close()
            } catch (_: Exception) {}
        }
    }

    fun fetchChangelog(url: String = "https://kanha321.github.io/Upastithi/Changelog.md") {
        if (changelogText != null || isChangelogLoading) return
        isChangelogLoading = true
        scope.launch {
            try {
                val response: HttpResponse = httpClient.get(url)
                val text = response.bodyAsText()
                withContext(Dispatchers.Main) {
                    changelogText = text
                    isChangelogLoading = false
                }
            } catch (e: Exception) {
                Log.e("Updater", "Failed to fetch changelog: ${e.message}")
                withContext(Dispatchers.Main) {
                    isChangelogLoading = false
                }
            }
        }
    }

    fun startDownload(context: Context) {
        val currentUpdate = update ?: return
        val downloadUrl = currentUpdate.downloadUrl.ifBlank {
            currentUpdate.downloadMCA3.ifBlank { currentUpdate.downloadMCA1 }
        }

        if (downloadUrl.isBlank()) {
            downloadState = UpdateDownloadState.Error("Invalid download URL. Please download from the website.")
            return
        }

        downloadJob?.cancel()
        downloadProgress = 0f
        bytesDownloaded = 0L
        totalBytes = currentUpdate.fileSizeBytes
        downloadState = UpdateDownloadState.Connecting

        downloadJob = scope.launch {
            var tempFile: File? = null
            var finalFile: File? = null

            try {
                val updatesDir = File(context.cacheDir, "updates")
                if (!updatesDir.exists()) updatesDir.mkdirs()

                val fileName = "Upasthiti-${currentUpdate.latestVersionName.ifBlank { "update" }}.apk"
                finalFile = File(updatesDir, fileName)
                tempFile = File(updatesDir, "$fileName.download.tmp")

                if (tempFile.exists()) tempFile.delete()
                if (finalFile.exists()) finalFile.delete()

                // Storage pre-check
                val requiredBytes = if (currentUpdate.fileSizeBytes > 0) currentUpdate.fileSizeBytes else 70L * 1024 * 1024
                if (context.cacheDir.usableSpace < requiredBytes + (10L * 1024 * 1024)) {
                    withContext(Dispatchers.Main) {
                        downloadState = UpdateDownloadState.Error("Insufficient storage space on device.")
                    }
                    return@launch
                }

                Log.d("Updater", "Starting fast OkHttp download for URL: $downloadUrl")
                val request = Request.Builder()
                    .url(downloadUrl)
                    .header("User-Agent", "Upasthiti-Android-App")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Server returned HTTP ${response.code}: ${response.message}")
                }

                val body = response.body ?: throw IllegalStateException("Response body is empty")
                val contentLength = if (body.contentLength() > 0) body.contentLength() else currentUpdate.fileSizeBytes

                withContext(Dispatchers.Main) {
                    totalBytes = contentLength
                    downloadState = UpdateDownloadState.Downloading(0f, 0L, contentLength)
                }

                val digest = MessageDigest.getInstance("SHA-256")

                BufferedInputStream(body.byteStream(), 131072).use { input ->
                    BufferedOutputStream(FileOutputStream(tempFile), 131072).use { output ->
                        val buffer = ByteArray(131072) // 128 KB buffer for maximum throughput
                        var downloaded = 0L
                        var lastUpdateTime = 0L
                        var readBytes = input.read(buffer)

                        while (coroutineContext.isActive && readBytes != -1) {
                            output.write(buffer, 0, readBytes)
                            digest.update(buffer, 0, readBytes)
                            downloaded += readBytes

                            val now = System.currentTimeMillis()
                            if (now - lastUpdateTime >= 100 || downloaded == contentLength) {
                                lastUpdateTime = now
                                val progress = if (contentLength > 0) downloaded.toFloat() / contentLength.toFloat() else 0f
                                val currentDownloaded = downloaded

                                withContext(Dispatchers.Main) {
                                    bytesDownloaded = currentDownloaded
                                    downloadProgress = progress
                                    downloadState = UpdateDownloadState.Downloading(progress, currentDownloaded, contentLength)
                                }
                            }
                            readBytes = input.read(buffer)
                        }

                        output.flush()
                    }
                }

                if (!coroutineContext.isActive) {
                    tempFile.delete()
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    downloadState = UpdateDownloadState.Verifying
                }

                // SHA-256 Verification
                val rawExpectedHash = currentUpdate.sha256.removePrefix("sha256:").trim().lowercase()
                if (rawExpectedHash.isNotBlank()) {
                    val computedHash = digest.digest().joinToString("") { "%02x".format(it) }
                    Log.d("Updater", "SHA-256 Validation: Computed=[$computedHash], Expected=[$rawExpectedHash]")

                    if (!computedHash.equals(rawExpectedHash, ignoreCase = true)) {
                        Log.e("Updater", "Checksum mismatch! Computed: $computedHash, Expected: $rawExpectedHash")
                        tempFile.delete()
                        withContext(Dispatchers.Main) {
                            downloadState = UpdateDownloadState.Error(
                                "Download corrupted (checksum mismatch). Please try again or download via browser."
                            )
                        }
                        return@launch
                    }
                }

                // Atomically rename temp file to final APK
                if (!tempFile.renameTo(finalFile)) {
                    tempFile.copyTo(finalFile, overwrite = true)
                    tempFile.delete()
                }

                withContext(Dispatchers.Main) {
                    downloadState = UpdateDownloadState.ReadyToInstall(finalFile)
                    installApk(context, finalFile)
                }

            } catch (e: Exception) {
                Log.e("Updater", "Download error: ${e.message}", e)
                tempFile?.let { if (it.exists()) it.delete() }
                withContext(Dispatchers.Main) {
                    downloadState = UpdateDownloadState.Error(
                        e.localizedMessage ?: "Network error during download. Please retry."
                    )
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        downloadState = UpdateDownloadState.Idle
        downloadProgress = 0f
        bytesDownloaded = 0L
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                downloadState = UpdateDownloadState.Error("APK file not found. Please download again.")
                return
            }

            // Check Unknown Sources Permission for Android 8.0+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(permissionIntent)
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("Updater", "Failed to launch package installer: ${e.message}", e)
            downloadState = UpdateDownloadState.Error(
                "Unable to launch installer: ${e.localizedMessage}. Please install manually via browser."
            )
        }
    }
}
