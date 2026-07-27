package com.kanhaji.basics.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun KTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    placeholders: List<String> = emptyList(),
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    suggestions: List<String> = emptyList()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValueState.text) {
            textFieldValueState = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    fun requestInputFocus() {
        focusRequester.requestFocus()
        keyboardController?.show()
        coroutineScope.launch {
            kotlinx.coroutines.delay(150)
            bringIntoViewRequester.bringIntoView()
        }
    }

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        label = "borderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
        label = "containerColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        Surface(
            onClick = { requestInputFocus() },
            shape = RoundedCornerShape(16.dp),
            color = containerColor,
            border = BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { requestInputFocus() }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        val activePlaceholders = remember(placeholder, placeholders, suggestions) {
                            val list = mutableListOf<String>()
                            if (placeholders.isNotEmpty()) {
                                list.addAll(placeholders)
                            } else if (suggestions.isNotEmpty()) {
                                suggestions.forEach { s ->
                                    if (s.isNotBlank()) list.add("e.g. $s")
                                }
                            } else if (!placeholder.isNullOrBlank()) {
                                list.add(placeholder)
                            }
                            list.distinct()
                        }

                        var currentIndex by remember { mutableIntStateOf(0) }

                        LaunchedEffect(activePlaceholders, value.isEmpty()) {
                            if (value.isEmpty() && activePlaceholders.size > 1) {
                                while (true) {
                                    delay(2000)
                                    currentIndex = (currentIndex + 1) % activePlaceholders.size
                                }
                            }
                        }

                        if (activePlaceholders.isNotEmpty()) {
                            val currentText = activePlaceholders.getOrElse(currentIndex % activePlaceholders.size) {
                                placeholder ?: "Enter $label..."
                            }
                            AnimatedContent(
                                targetState = currentText,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn(tween(250)))
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut(tween(250)))
                                },
                                label = "placeholderCarousel"
                            ) { hint ->
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = placeholder ?: "Enter $label...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    BasicTextField(
                        value = textFieldValueState,
                        onValueChange = { newValue ->
                            textFieldValueState = newValue
                            if (newValue.text != value) {
                                onValueChange(newValue.text)
                            }
                        },
                        enabled = enabled,
                        readOnly = readOnly,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = singleLine,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        interactionSource = interactionSource,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        visualTransformation = visualTransformation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                trailingIcon?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { requestInputFocus() }
                    ) {
                        it()
                    }
                }
            }
        }

        val currentMatches = remember(value, suggestions) {
            if (suggestions.isEmpty()) emptyList()
            else {
                suggestions.filter { suggestion ->
                    suggestion.contains(value, ignoreCase = true) && !suggestion.equals(value, ignoreCase = true)
                }.take(6)
            }
        }

        var lastValidSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
        if (currentMatches.isNotEmpty()) {
            lastValidSuggestions = currentMatches
        }

        val showSuggestions = isFocused && currentMatches.isNotEmpty()

        LaunchedEffect(showSuggestions) {
            if (showSuggestions) {
                kotlinx.coroutines.delay(100)
                bringIntoViewRequester.bringIntoView()
            }
        }

        AnimatedVisibility(
            visible = showSuggestions,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(180)
            )
        ) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    lastValidSuggestions.forEach { suggestion ->
                        Surface(
                            onClick = {
                                val acceptedText = suggestion
                                textFieldValueState = TextFieldValue(
                                    text = acceptedText,
                                    selection = TextRange(acceptedText.length)
                                )
                                onValueChange(acceptedText)
                                requestInputFocus()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
