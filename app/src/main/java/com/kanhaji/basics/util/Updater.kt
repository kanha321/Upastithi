package com.kanhaji.basics.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.upasthiti.util.Course
import com.kanhaji.upasthiti.util.UpasthitiUtils
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.File

object Updater {

    var downloadProgress by mutableFloatStateOf(0f)
    var isDownloading by mutableStateOf<Boolean?>(null)
    var update by mutableStateOf<Update?>(null)

    suspend fun startDownload(context: Context) {
        val course = UpasthitiUtils.course
        if (update == null) {
            println("No update data available.")
            return
        }

        isDownloading = true

        val downloadUrl = when {
            update!!.downloadUrl.isNotBlank() -> update!!.downloadUrl
            course == Course.MCA3 && update!!.downloadMCA3.isNotBlank() -> update!!.downloadMCA3
            update!!.downloadMCA1.isNotBlank() -> update!!.downloadMCA1
            else -> update!!.downloadMCA3.ifBlank { update!!.downloadMCA1 }
        }

        if (downloadUrl.isBlank()) {
            isDownloading = false
            println("No valid download URL found in update payload.")
            return
        }

        try {
            // Make request
            val response: HttpResponse = httpClient.get(downloadUrl)

            val contentLength = response.contentLength() ?: -1L
            val fileName = downloadUrl.substringAfterLast("/")
            val outputFile = File(context.cacheDir, fileName)

            outputFile.outputStream().use { fos ->
                val channel: ByteReadChannel = response.bodyAsChannel()
                var bytesRead = 0L
                val buffer = ByteArray(8192)

                while (!channel.isClosedForRead) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read == -1) break
                    fos.write(buffer, 0, read)

                    bytesRead += read
                    if (contentLength > 0) {
                        downloadProgress = bytesRead.toFloat() / contentLength.toFloat()
                        println("Download progress: ${"%.2f".format(downloadProgress * 100)}%")
                    }
                }
            }

            println("Download complete: ${outputFile.absolutePath}")
            isDownloading = false
            downloadProgress = 1f

            // ✅ Launch package installer
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // must match the provider authority in manifest
                outputFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            downloadProgress = 0f
            isDownloading = false
        }
    }
}
