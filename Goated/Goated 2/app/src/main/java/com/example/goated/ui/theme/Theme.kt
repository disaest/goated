package com.example.goated.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AccentBlue,
    onPrimary = White,
    secondary = PrimaryBlue,
    onSecondary = White,
    background = DarkBackground,
    onBackground = White,
    surface = CardBackground,
    onSurface = White,
    outline = CardBorder
)

@Composable
fun GoatedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}