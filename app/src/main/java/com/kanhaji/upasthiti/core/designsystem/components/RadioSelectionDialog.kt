package com.kanhaji.upasthiti.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class RadioOption(
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun RadioSelectionDialog(
    title: String,
    icon: ImageVector? = null,
    options: List<RadioOption>,
    initialSelection: Int = 0,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(initialSelection.coerceAtLeast(0)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                options.forEachIndexed { index, option ->
                    TextButton(
                        onClick = {
                            selectedIndex = index
                            option.onClick()
                        }
                    ) {
                        Text(
                            text = option.label,
                            color = if (selectedIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
