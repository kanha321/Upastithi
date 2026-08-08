package com.kanhaji.upasthiti.features.home.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Center,
    charDelayMs: Long = 30L
) {
    var visibleCharCount by remember(text) { mutableIntStateOf(0) }
    var cursorVisible by remember { mutableStateOf(true) }

    // Character typing animation loop
    LaunchedEffect(text) {
        visibleCharCount = 0
        for (i in 1..text.length) {
            visibleCharCount = i
            delay(charDelayMs)
        }
    }

    // Discrete 1 or 0 blink toggle (450ms interval, instant step change)
    LaunchedEffect(Unit) {
        while (true) {
            delay(450L)
            cursorVisible = !cursorVisible
        }
    }

    val currentText = text.take(visibleCharCount)
    val cursorAlpha = if (cursorVisible) 1.0f else 0.0f

    val annotatedString = remember(currentText, cursorAlpha) {
        buildAnnotatedString {
            append(currentText)
            append(" ")
            withStyle(SpanStyle(color = color.copy(alpha = cursorAlpha))) {
                append("▋")
            }
        }
    }

    Text(
        text = annotatedString,
        style = style.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        ),
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}
