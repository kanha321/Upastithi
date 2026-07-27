package com.kanhaji.upastithi.screen.edit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.basics.composables.KTextField

import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.remember
import com.kanhaji.upastithi.data.TimeTableManager

@Composable
fun EditCourseInfoCard(
    courseCode: String,
    onCourseCodeChanged: (String) -> Unit,
    courseName: String,
    onCourseNameChanged: (String) -> Unit,
    isPractical: Boolean,
    onPracticalToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val courseCodeSuggestions = remember { TimeTableManager.getAllCourseCodes() }
    val courseNameSuggestions = remember { TimeTableManager.getAllCourseNames() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Course Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            KTextField(
                value = courseCode,
                onValueChange = { newCode ->
                    onCourseCodeChanged(newCode)
                    val matchingName = TimeTableManager.getCourseName(newCode)
                    if (matchingName.isNotBlank() && !matchingName.equals(newCode, ignoreCase = true)) {
                        onCourseNameChanged(matchingName)
                    }
                },
                label = "Course Code",
                placeholder = "e.g. CS35101",
                leadingIcon = Icons.Default.Code,
                suggestions = courseCodeSuggestions,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Course Code",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            KTextField(
                value = courseName,
                onValueChange = { newName ->
                    onCourseNameChanged(newName)
                    val matchingCode = TimeTableManager.getCourseCode(newName)
                    if (!matchingCode.isNullOrBlank()) {
                        onCourseCodeChanged(matchingCode)
                    }
                },
                label = "Course Title",
                placeholder = "e.g. Multimedia Technology",
                leadingIcon = Icons.Default.Book,
                suggestions = courseNameSuggestions,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Course Title",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Text(
                text = "Class Type",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val lectureColor by animateColorAsState(
                    targetValue = if (!isPractical) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "lectureColor"
                )
                val labColor by animateColorAsState(
                    targetValue = if (isPractical) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "labColor"
                )

                Surface(
                    onClick = { onPracticalToggled(false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = lectureColor,
                    border = BorderStroke(
                        1.dp,
                        if (!isPractical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Class,
                            contentDescription = "Lecture",
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 6.dp),
                            tint = if (!isPractical) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Lecture",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (!isPractical) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = { onPracticalToggled(true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = labColor,
                    border = BorderStroke(
                        1.dp,
                        if (isPractical) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Practical",
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 6.dp),
                            tint = if (isPractical) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Practical / Lab",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPractical) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
