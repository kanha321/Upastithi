package com.kanhaji.basics.legacy

//import com.kanhaji.basics.networking.httpClient
//import com.mwi.frontend.entity.DashBuildResult
//import io.ktor.client.call.body
//import io.ktor.client.request.forms.InputProvider
//import io.ktor.client.request.forms.MultiPartFormDataContent
//import io.ktor.client.request.forms.formData
//import io.ktor.client.request.post
//import io.ktor.client.request.setBody
//import io.ktor.http.ContentType
//import io.ktor.http.Headers
//import io.ktor.http.HttpHeaders
//import io.ktor.utils.io.core.Input
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.io.File
//import java.util.Locale
//import kotlin.collections.chunked
//import kotlin.collections.forEach
//
//
//// Guess reasonable content types for manifest, thumbnail, and segments
//private fun guessContentType(file: File): ContentType =
//    when (file.extension.lowercase(Locale.ROOT)) {
//        "mpd" -> ContentType.parse("application/dash+xml")
//        "m3u8" -> ContentType.parse("application/vnd.apple.mpegurl")
//        "png" -> ContentType.Image.PNG
//        "jpg", "jpeg" -> ContentType.Image.JPEG
//        "webp" -> ContentType.parse("image/webp")
//        "gif" -> ContentType.Image.GIF
//        "m4s" -> ContentType.parse("video/mp4")
//        "mp4" -> ContentType.parse("video/mp4")
//        else -> ContentType.Application.OctetStream
//    }
//
//// POST /api/videos/create
//private suspend fun createVideoRecord(
//    baseUrl: String,
//    uId: String,
//    title: String,
//    description: String,
//    durationMs: Long,
//    playlist: File,
//    thumbnail: File
//): String {
//    val url = "$baseUrl/api/videos/create"
//    return httpClient.post(url) {
//        setBody(
//            MultiPartFormDataContent(
//                formData {
//                    append("uId", uId)
//                    append("title", title)
//                    append("description", description)
//                    append("duration", durationMs.toString())
//
//                    append(
//                        "playlist",
//                        InputProvider { playlist.inputStream() as Input },
//                        Headers.build {
//                            append(
//                                HttpHeaders.ContentType,
//                                guessContentType(playlist).toString()
//                            )
//                            append(
//                                HttpHeaders.ContentDisposition,
//                                "filename=\"${playlist.name}\""
//                            )
//                        }
//                    )
//
//                    append(
//                        "thumbnail",
//                        InputProvider { thumbnail.inputStream() as Input },
//                        Headers.build {
//                            append(
//                                HttpHeaders.ContentType,
//                                guessContentType(thumbnail).toString()
//                            )
//                            append(
//                                HttpHeaders.ContentDisposition,
//                                "filename=\"${thumbnail.name}\""
//                            )
//                        }
//                    )
//                }
//            )
//        )
//    }.body()
//}
//
//// POST /api/videos/{videoId}/segments
//private suspend fun uploadSegmentBatch(
//    baseUrl: String,
//    videoId: String,
//    files: List<File>
//) {
//    val url = "$baseUrl/api/videos/$videoId/segments"
//    httpClient.post(url) {
//        setBody(
//            MultiPartFormDataContent(
//                formData {
//                    files.forEach { file ->
//                        append(
//                            "files",
//                            InputProvider { file.inputStream() as Input },
//                            Headers.build {
//                                append(
//                                    HttpHeaders.ContentType,
//                                    guessContentType(file).toString()
//                                )
//                                append(
//                                    HttpHeaders.ContentDisposition,
//                                    "filename=\"${file.name}\""
//                                )
//                            }
//                        )
//                    }
//                }
//            )
//        )
//    }
//}
//
//// POST /api/videos/{videoId}/finalize
//private suspend fun finalizeUpload(
//    baseUrl: String,
//    videoId: String
//) {
//    val url = "$baseUrl/api/videos/$videoId/finalize"
//    httpClient.post(url) { /* no body */ }
//}
//
///**
// * Orchestrator: create -> upload segments (in batches) -> finalize
// *
// * Returns Result\<videoId\> on success.
// */
//suspend fun uploadDashToBackend(
//    baseUrl: String,
//    uId: String,
//    title: String,
//    description: String,
//    dash: DashBuildResult,
//    batchSize: Int = 25
//): Result<String> = withContext(Dispatchers.IO) {
//    runCatching {
//        // 1) Create the video record with manifest (as `playlist`) and `thumbnail`
//        val videoId = createVideoRecord(
//            baseUrl = baseUrl,
//            uId = uId,
//            title = title,
//            description = description,
//            durationMs = dash.durationMs,
//            playlist = dash.manifest,
//            thumbnail = dash.thumbnail
//        )
//
//        // 2) Upload segments in batches
//        dash.segments.chunked(batchSize).forEach { batch ->
//            uploadSegmentBatch(
//                baseUrl = baseUrl,
//                videoId = videoId,
//                files = batch
//            )
//        }
//
//        // 3) Finalize
//        finalizeUpload(baseUrl, videoId)
//
//        videoId
//    }
//}