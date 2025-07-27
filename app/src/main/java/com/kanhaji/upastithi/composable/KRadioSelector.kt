package com.kanhaji.upastithi.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kanhaji.upastithi.data.attendance.AttendanceStatus

data class AttendanceItem(
    val status: AttendanceStatus,
    val icon: ImageVector,
    val highLightColor: Color
)

@Composable
fun KRadioSelector(
    items: List<AttendanceItem>,
    initialSelection: String? = null,
    onSelectionChanged: (AttendanceItem?) -> Unit
) {
    var selectedItem by remember { mutableStateOf(initialSelection) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            val itemDisplayName = item.status.displayName
            val isSelected = selectedItem == itemDisplayName

            val animatedBorderWidth = animateBorderWidth(isSelected = isSelected)
            val animatedContainerColor = animateSelectionColor(
                isSelected = isSelected,
                selectedColor = item.highLightColor.copy(alpha = 0.1f),
                unselectedColor = MaterialTheme.colorScheme.surface
            )
            val animatedContentColor = animateSelectionColor(
                isSelected = isSelected,
                selectedColor = item.highLightColor,
                unselectedColor = MaterialTheme.colorScheme.onSurface
            )
            val animatedTextScale = animateTextScale(isSelected = isSelected)

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = animatedBorderWidth,
                        color = if (isSelected) item.highLightColor else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ),
                onClick = {
                    selectedItem = if (isSelected) null else itemDisplayName
                    onSelectionChanged(if (isSelected) null else item)
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = animatedContainerColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = itemDisplayName,
                        modifier = Modifier.size(24.dp),
                        tint = animatedContentColor
                    )
                    Text(
                        text = itemDisplayName,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .scale(animatedTextScale),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = animatedContentColor
                    )
                }
            }
        }
    }
}