package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PuzzleForgeColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBg,
    primaryContainer = Color(0xFF00384D),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricPurple,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = AmberGold,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = NeonRed,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cyber game aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PuzzleForgeColorScheme,
        typography = Typography,
        content = content
    )
}
