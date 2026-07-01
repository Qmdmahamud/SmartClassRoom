package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicDarkColorScheme = darkColorScheme(
    primary = ElectricViolet,
    secondary = CyanAura,
    tertiary = CyanAura,
    background = DeepCharcoal,
    surface = CosmicSlate,
    onPrimary = Color.White,
    onSecondary = DeepCharcoal,
    onTertiary = DeepCharcoal,
    onBackground = OffWhite,
    onSurface = OffWhite,
    surfaceVariant = MutedMolybdenum,
    onSurfaceVariant = OffWhite,
    outline = MutedMolybdenum,
    error = AlertRed
)

// We force the theme to use the gorgeous dark Cosmic Slate theme as requested!
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode to reflect the gorgeous premium Cosmic Slate theme!
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce the specific brand guidelines!
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
