package com.kanhaji.upastithi.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.kanhaji.upastithi.features.home.data.AttendanceStatus

data class AttendanceItem(
    val status: AttendanceStatus,
    val icon: ImageVector,
    val highLightColor: Color = status.color
)

@Composable
fun KRadioSelector(
    items: List<AttendanceItem>,
    initialSelection: String? = null,
    gridColumns: Int = 1,
    onSelectionChanged: (AttendanceItem?) -> Unit
) {
    var selectedItem by remember { mutableStateOf(initialSelection) }

    if (gridColumns > 1) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.chunked(gridColumns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        val itemDisplayName = item.status.displayName
                        val isSelected = selectedItem == itemDisplayName
                        RadioItemCard(
                            item = item,
                            isSelected = isSelected,
                            gridMode = true,
                            onClick = {
                                selectedItem = if (isSelected) null else itemDisplayName
                                onSelectionChanged(if (isSelected) null else item)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size < gridColumns) {
                        repeat(gridColumns - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                val itemDisplayName = item.status.displayName
                val isSelected = selectedItem == itemDisplayName
                RadioItemCard(
                    item = item,
                    isSelected = isSelected,
                    gridMode = false,
                    onClick = {
                        selectedItem = if (isSelected) null else itemDisplayName
                        onSelectionChanged(if (isSelected) null else item)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RadioItemCard(
    item: AttendanceItem,
    isSelected: Boolean,
    gridMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemDisplayName = item.status.displayName
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
        modifier = modifier.border(
            width = animatedBorderWidth,
            color = if (isSelected) item.highLightColor else MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(12.dp)
        ),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = animatedContainerColor
        )
    ) {
        if (gridMode) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = itemDisplayName,
                    modifier = Modifier.size(28.dp),
                    tint = animatedContentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = itemDisplayName,
                    modifier = Modifier.scale(animatedTextScale),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = animatedContentColor
                )
            }
        } else {
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
