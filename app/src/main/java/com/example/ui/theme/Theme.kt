package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = DeepPurpleDark,
    primaryContainer = EmeraldGreenDark,
    onPrimaryContainer = CrispWhite,
    secondary = AmberGold,
    onSecondary = DeepPurpleDark,
    secondaryContainer = DeepPurpleElevated,
    onSecondaryContainer = CrispWhite,
    tertiary = CyanAccent,
    background = DeepPurpleDark,
    onBackground = CrispWhite,
    surface = DeepPurpleMid,
    onSurface = CrispWhite,
    surfaceVariant = DeepPurpleElevated,
    onSurfaceVariant = SlateGrayText,
    outline = DeepPurpleBorder,
    outlineVariant = Color(0xFF4338CA),
    error = CoralRed,
    onError = CrispWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce our strict brand palette
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
