package com.kanhaji.basics.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kanhaji.basics.entity.Update
import com.kanhaji.basics.networking.httpClient
import com.kanhaji.upastithi.AndroidContext
import com.kanhaji.upastithi.util.Course
import com.kanhaji.upastithi.util.UpasthitiUtils
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
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

        val downloadUrl = when (course) {
            Course.MCA1 -> update!!.downloadMCA1
            Course.MCA3 -> update!!.downloadMCA3
        }

        try {
            // Make request
            val response: HttpResponse = httpClient.get(downloadUrl)

            // Get file size from headers
            val contentLength = response.contentLength() ?: -1L

            // Decide where to save APK
            val fileName = downloadUrl.substringAfterLast("/")
            val outputFile = File(context.cacheDir, fileName)

            // Create output stream
            outputFile.outputStream().use { fos ->
                val channel: ByteReadChannel = response.bodyAsChannel()
                var bytesRead = 0L
                val buffer = ByteArray(8192) // 8 KB buffer

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

        } catch (e: Exception) {
            e.printStackTrace()
            downloadProgress = 0f
        }
    }
}