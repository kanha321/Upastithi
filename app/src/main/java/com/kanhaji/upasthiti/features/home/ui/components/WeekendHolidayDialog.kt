package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kanhaji.upasthiti.util.UpasthitiUtils
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private val weekendVibeMessages = listOf(
    "🎉 Weekend Mode ON! Time to recharge, code your dream side project, or just chill.",
    "☕ Saturdays & Sundays are for sleeping in, coffee, and ZERO lectures!",
    "🏖️ No classes today! Go out, relax, or catch up on your favorite movies & shows.",
    "🎮 404: Classes not found on weekends. Go enjoy your free time!",
    "🍕 High energy, low stress! Grab your favorite food and make the most of the weekend.",
    "🎧 No alarms, no lectures, no attendance pressure. Enjoy your day off!",
    "🚀 Rest is fuel for greatness. Unwind today and come back stronger on Monday!"
)

@Composable
fun WeekendHolidayDialog(
    date: LocalDate,
    onDismiss: () -> Unit
) {
    val dateFormatted = remember(date) {
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val dayName = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        "$dayName, ${date.dayOfMonth} $monthName ${date.year}"
    }

    val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
    val titleText = if (isSunday) "Sunday Chill Mode ☀️" else "Saturday Vibes 🎉"
    val randomMessage = remember(date) {
        val combinedMessages = weekendVibeMessages + UpasthitiUtils.noClassesMessages
        combinedMessages.random()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Outlined Header Icon Card
                OutlinedCard(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSunday) Icons.Default.BeachAccess else Icons.Default.Celebration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Outlined Message Card
                OutlinedCard(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypewriterText(
                            text = randomMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Outlined Action Button
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.tertiary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSunday) "Enjoy Sunday! 😎" else "Enjoy Saturday! 🥳",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
