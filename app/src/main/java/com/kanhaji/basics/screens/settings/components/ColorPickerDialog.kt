package com.kanhaji.basics.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.kanhaji.basics.theme.ThemeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ColorPickerDialog(
    customColor: MutableState<Color>,
    initialColor: Color = ThemeManager.customColor.value,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    // Remember the color before user starts editing
    val originalColor = remember { customColor.value }

    var selectedColor by remember { mutableStateOf(customColor.value) }
    var hexCode by remember { mutableStateOf(colorToHex(customColor.value)) }

    val controller = rememberColorPickerController()
    val scope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    // Sync hex field with local color
    LaunchedEffect(selectedColor) {
        val newHex = colorToHex(selectedColor)
        if (newHex != hexCode) {
            hexCode = newHex
        }
    }

    // Sync picker when hex changes
    LaunchedEffect(hexCode) {
        if (hexCode.length == 6) {
            try {
                val newColor = hexToColor(hexCode)
                if (newColor != selectedColor) {
                    selectedColor = newColor
                    controller.selectByColor(newColor, fromUser = false)

                    // Apply after debounce
                    debounceJob?.cancel()
                    debounceJob = scope.launch {
                        delay(150)
                        ThemeManager.customColor.value = newColor
                        customColor.value = newColor
                        onColorSelected(newColor)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    AlertDialog(
        onDismissRequest = {
            // Restore original color if canceled via outside tap
            ThemeManager.customColor.value = originalColor
            customColor.value = originalColor
            onDismiss()
        },
        title = { Text("Pick a Color") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // HSV Picker
                HsvColorPicker(
                    initialColor = initialColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(10.dp),
                    controller = controller,
                    onColorChanged = { envelope ->
                        selectedColor = envelope.color
                        debounceJob?.cancel()
                        debounceJob = scope.launch {
                            delay(150)
                            ThemeManager.customColor.value = selectedColor
                            customColor.value = selectedColor
                            onColorSelected(selectedColor)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hex input
                OutlinedTextField(
                    value = hexCode,
                    onValueChange = { newHex ->
                        val filteredHex = newHex.filter { it.isDigit() || it in "abcdefABCDEF" }
                        if (filteredHex.length <= 6) {
                            hexCode = filteredHex.uppercase()
                        }
                    },
                    label = { Text("Hex Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii
                    ),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(selectedColor, shape = CircleShape)
                                .border(1.dp, Color.Gray, CircleShape)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                // Restore the original color
                ThemeManager.customColor.value = originalColor
                customColor.value = originalColor
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return (argb and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()
}

fun hexToColor(hex: String): Color {
    return try {
        val parsedColor = hex.toLong(16) or 0xFF000000
        Color(parsedColor)
    } catch (e: Exception) {
        Color(0xFF8F4C38)
    }
}
