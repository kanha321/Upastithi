package com.kanhaji.basics.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RadioItem(
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun RadioSelectionDialog(
    title: String,
    options: List<RadioItem>,
    initialSelection: Int? = null,
    icon: ImageVector = Icons.Outlined.Add,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIndex by rememberSaveable { mutableStateOf(initialSelection) }

    val dismissInteractionSource = remember { MutableInteractionSource() }
    val confirmInteractionSource = remember { MutableInteractionSource() }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = "Icon",
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(options) { index, item ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val radioInteractionSource = remember { MutableInteractionSource() }

                    Surface(
                        onClick = {
                            selectedIndex = index
                            item.onClick()
                        },
                        interactionSource = interactionSource,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    selectedIndex = index
                                    item.onClick()
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedIndex == index,
                                onClick = {
                                    selectedIndex = index
                                    item.onClick()
                                },
                                interactionSource = radioInteractionSource,
                                colors = RadioButtonDefaults.colors(
                                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = item.label,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            KButton(
                onClick = onConfirm,
                interactionSource = confirmInteractionSource
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            KTextButton(
                onClick = onDismiss,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color.Transparent,
//                    contentColor = MaterialTheme.colorScheme.primary
//                ),
                interactionSource = dismissInteractionSource
            ) {
                Text("Cancel")
            }
        }
    )
}

