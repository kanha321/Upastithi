package com.kanhaji.basics.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun KTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    val tabShape = RoundedCornerShape(20.dp)

    Surface(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 6.dp),
        shape = tabShape,
        color = if (selected) selectedColor.copy(alpha = 0.2f) else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = text,
                color = if (selected) selectedColor else unselectedColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
