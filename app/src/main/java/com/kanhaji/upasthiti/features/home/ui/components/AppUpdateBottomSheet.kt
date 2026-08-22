package com.kanhaji.upasthiti.features.home.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import com.kanhaji.basics.entity.UpdatePriority
import com.kanhaji.basics.util.UpdateDownloadState
import com.kanhaji.basics.util.Updater
import com.kanhaji.upasthiti.core.designsystem.components.MarkdownViewer
import com.kanhaji.upasthiti.util.KToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUpdate = Updater.update
    val isForceUpdate = Updater.isForceUpdate
    val downloadState = Updater.downloadState
    val changelogText = Updater.changelogText
    val isChangelogLoading = Updater.isChangelogLoading

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        Updater.fetchChangelog(
            if (currentUpdate != null && currentUpdate.changelog.isNotBlank()) currentUpdate.changelog
            else "https://kanha321.github.io/Upastithi/Changelog.md"
        )
    }

    val formattedSize = remember(currentUpdate?.fileSizeBytes) {
        val bytes = currentUpdate?.fileSizeBytes ?: 0L
        if (bytes <= 0) "16.8 MB"
        else "%.1f MB".format(bytes.toFloat() / (1024f * 1024f))
    }

    val targetProgress = when (downloadState) {
        is UpdateDownloadState.Downloading -> downloadState.progress
        is UpdateDownloadState.Verifying, is UpdateDownloadState.ReadyToInstall -> 1f
        else -> 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "DownloadProgressAnimation"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Title Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isForceUpdate) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Row(
                        modifier = Modifier.size(48.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isForceUpdate) Icons.Default.SystemUpdate else Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = if (isForceUpdate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isForceUpdate) "Update Required" else "Update Available!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = currentUpdate?.latestVersionName?.ifBlank { "Latest" } ?: "Latest",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = formattedSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Changelog Section Card
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val dotColor = when (Updater.updatePriority) {
                                UpdatePriority.OPTIONAL -> Color(0xFF4CAF50)     // Green
                                UpdatePriority.RECOMMENDED -> Color(0xFFFFB300) // Yellow / Amber
                                UpdatePriority.CRITICAL -> Color(0xFFE53935)       // Red
                            }

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(dotColor, CircleShape)
                            )

                            Text(
                                text = "What's New in this Release",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (isChangelogLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 220.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (!changelogText.isNullOrBlank()) {
                            MarkdownViewer(markdown = changelogText!!)
                        } else if (isChangelogLoading) {
                            Text(
                                text = "Loading release notes...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Text(
                                text = "Includes performance improvements, Saturday class schedules, and enhanced timetable stability.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            // Progress & State UI
            when (downloadState) {
                is UpdateDownloadState.Connecting -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = "Connecting to download server...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is UpdateDownloadState.Downloading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            strokeCap = StrokeCap.Round
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val currentMB = "%.1f".format(downloadState.bytesDownloaded.toFloat() / (1024f * 1024f))
                            val totalMB = if (downloadState.totalBytes > 0) "%.1f MB".format(downloadState.totalBytes.toFloat() / (1024f * 1024f)) else formattedSize
                            Text(
                                text = "Downloading: $currentMB / $totalMB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(animatedProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                is UpdateDownloadState.Verifying -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Verifying package checksum (SHA-256)...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                is UpdateDownloadState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = downloadState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                else -> {}
            }

            Spacer(Modifier.height(4.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (downloadState) {
                    is UpdateDownloadState.Connecting, is UpdateDownloadState.Downloading -> {
                        OutlinedButton(
                            onClick = { Updater.cancelDownload() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Cancel Download")
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Button(
                            onClick = { Updater.installApk(context, downloadState.apkFile) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Install Update", fontWeight = FontWeight.Bold)
                        }
                    }

                    is UpdateDownloadState.Error -> {
                        Button(
                            onClick = { Updater.startDownload(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text("Retry Download", fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        Button(
                            onClick = { Updater.startDownload(context) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(if (isForceUpdate) "Update Now (Required)" else "Update Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isForceUpdate) "Close" else "Later")
                }

                // Fallback Browser Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            try {
                                val url = currentUpdate?.downloadUrl?.ifBlank {
                                    "https://github.com/kanha321/Upastithi/releases"
                                } ?: "https://github.com/kanha321/Upastithi/releases"
                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                KToast.show(context, "Unable to open browser")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Download via Browser",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
