package com.alain.epure.pdf.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Épure PDF is always black with text only: no light theme, no dynamic colour.
private val EpureColorScheme = darkColorScheme(
    primary = TextPrimary,
    onPrimary = Black,
    secondary = TextSecondary,
    onSecondary = Black,
    background = Black,
    onBackground = TextPrimary,
    surface = Black,
    onSurface = TextPrimary
)

@Composable
fun EpureTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EpureColorScheme,
        typography = Typography,
        content = content
    )
}
