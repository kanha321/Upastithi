package com.kanhaji.basics.legacy

//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.ExperimentalAnimationApi
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.CloudUpload
//import androidx.compose.material.icons.filled.Schedule
//import androidx.compose.material.icons.filled.Timelapse
//import androidx.compose.material3.AssistChip
//import androidx.compose.material3.AssistChipDefaults
//import androidx.compose.material3.Button
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ElevatedCard
//import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
//import androidx.compose.material3.Icon
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedCard
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.Immutable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import androidx.lifecycle.lifecycleScope
//import com.mwi.frontend.screens.upload.UploadScreenModel
//import com.mwi.frontend.entity.DashBuildResult
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlin.math.roundToInt
//
//@Immutable
//data class UploadUiState(
//    val progress: Float = 0f,
//    val speedText: String = "—",
//    val elapsedText: String = "00:00",
//    val etaText: String = "—",
//    val sizeText: String = "—",
//    val statusText: String = "Waiting to upload"
//)
//
//@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
//@Composable
//fun UploadVideoComponent(
//    screenModel: UploadScreenModel,
//    dash: DashBuildResult,
//    baseUrl: String,
//    onCompleted: (String) -> Unit = {}
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    var ui by remember {
//        val totalBytes = dash.manifest.length() + dash.thumbnail.length() +
//            dash.segments.sumOf { it.length() }
//        mutableStateOf(
//            UploadUiState(
//                progress = 0f,
//                sizeText = "0 / ${screenModel.formatFileSize(totalBytes)}",
//                statusText = "Uploading to MWI"
//            )
//        )
//    }
//    var started by remember { mutableStateOf(false) }
//    var uploading by remember { mutableStateOf(false) }
//
//    LaunchedEffect(uploading) {
//        if (!uploading) return@LaunchedEffect
//        val start = System.currentTimeMillis()
//        while (uploading) {
//            val secs = ((System.currentTimeMillis() - start) / 1000L).coerceAtLeast(0)
//            ui = ui.copy(
//                elapsedText = screenModel.formatTime(secs),
//                etaText = "—"
//            )
//            delay(1000)
//        }
//    }
//
//    // Trigger once, run the upload in lifecycleScope (not canceled by composition changes)
//    LaunchedEffect(dash.outputDir, started) {
//        if (started) return@LaunchedEffect
//        started = true
//        uploading = true
//        ui = ui.copy(statusText = "Creating record...")
//
//        lifecycleOwner.lifecycleScope.launch {
//            val uId = screenModel.getDeviceId(context)
//            val result = screenModel.uploadDashToBackend(
//                baseUrl = baseUrl,
//                uId = uId,
//                title = screenModel.title,
//                description = screenModel.description,
//                dash = dash,
//                batchSize = 25
//            )
//
//            uploading = false
//            result.onSuccess { videoId ->
//                ui = ui.copy(
//                    progress = 1f,
//                    statusText = "Finalized"
//                )
//                onCompleted(videoId)
//            }.onFailure { e ->
//                ui = ui.copy(
//                    statusText = "Upload failed: ${e.message ?: "unknown error"}"
//                )
//            }
//        }
//    }
//
//    UploadVideoContent(state = ui)
//}
//
//@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
//@Composable
//fun UploadVideoContent(state: UploadUiState, modifier: Modifier = Modifier) {
//    val clamped = state.progress.coerceIn(0f, 1f)
//    val percentText = "${(clamped * 100f).roundToInt()}%"
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        ElevatedCard(
//            modifier = Modifier.fillMaxWidth(),
//            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
//            shape = RoundedCornerShape(24.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        Brush.linearGradient(
//                            listOf(
//                                MaterialTheme.colorScheme.primaryContainer,
//                                MaterialTheme.colorScheme.tertiaryContainer
//                            )
//                        )
//                    )
//                    .padding(20.dp)
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.CloudUpload,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                            modifier = Modifier.size(28.dp)
//                        )
//                        Text(
//                            text = state.statusText,
//                            style = MaterialTheme.typography.titleLarge,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(Modifier.weight(1f))
//                    }
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(20.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Box(
//                            modifier = Modifier.size(96.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator(
//                                progress = { clamped },
//                                modifier = Modifier.fillMaxSize(),
//                                strokeWidth = 10.dp,
//                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                            AnimatedContent(
//                                targetState = percentText,
//                                transitionSpec = { fadeIn() togetherWith fadeOut() }
//                            ) { txt ->
//                                Text(
//                                    text = txt,
//                                    style = MaterialTheme.typography.titleMedium,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            }
//                        }
//
//                        Column(
//                            modifier = Modifier.weight(1f),
//                            verticalArrangement = Arrangement.spacedBy(10.dp)
//                        ) {
//                            LinearProgressIndicator(
//                                progress = { clamped },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(10.dp)
//                                    .clip(RoundedCornerShape(100)),
//                                color = MaterialTheme.colorScheme.primary,
//                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
//                            )
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(
//                                    text = state.sizeText,
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
//                                Text(
//                                    text = state.speedText,
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onPrimaryContainer
//                                )
//                            }
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Icon(
//                                        Icons.Default.Schedule,
//                                        contentDescription = null,
//                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
//                                        modifier = Modifier.size(18.dp)
//                                    )
//                                    Spacer(Modifier.size(6.dp))
//                                    Text(
//                                        text = "ETA ${state.etaText}",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                                    )
//                                }
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Icon(
//                                        Icons.Default.Timelapse,
//                                        contentDescription = null,
//                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
//                                        modifier = Modifier.size(18.dp)
//                                    )
//                                    Spacer(Modifier.size(6.dp))
//                                    Text(
//                                        text = "Elapsed ${state.elapsedText}",
//                                        style = MaterialTheme.typography.bodySmall,
//                                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        OutlinedCard(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(20.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "Transfer steps",
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold
//                )
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    AssistChip(
//                        onClick = {},
//                        label = { Text("Prepare") },
//                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
//                        colors = AssistChipDefaults.assistChipColors(
//                            containerColor = MaterialTheme.colorScheme.secondaryContainer
//                        )
//                    )
//                    AssistChip(
//                        onClick = {},
//                        label = { Text("Upload") },
//                        leadingIcon = {
//                            Icon(
//                                if (clamped > 0.99f) Icons.Default.Check else Icons.Default.CloudUpload,
//                                contentDescription = null
//                            )
//                        }
//                    )
//                    AssistChip(
//                        onClick = {},
//                        label = { Text("Finalize") },
//                        leadingIcon = {
//                            Icon(
//                                if (clamped > 0.99f) Icons.Default.Check else Icons.Default.Schedule,
//                                contentDescription = null
//                            )
//                        }
//                    )
//                }
//            }
//        }
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            OutlinedButton(onClick = {}, enabled = false) { Text("Pause") }
//            Button(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) { Text("Cancel") }
//        }
//    }
//}