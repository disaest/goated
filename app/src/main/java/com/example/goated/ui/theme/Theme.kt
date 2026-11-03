package com.example.goated.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = White,
    secondary = GradientTop,
    onSecondary = White,
    background = DarkBackground,
    onBackground = White,
    surface = CardBackground,
    onSurface = White,
    surfaceVariant = CardBackground,
    onSurfaceVariant = SecondaryText,
    outline = FieldBorder,
    error = ErrorRed
)

@Composable
fun GoatedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
