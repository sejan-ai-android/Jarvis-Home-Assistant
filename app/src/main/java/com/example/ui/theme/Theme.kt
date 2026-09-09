package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = TitaniumVoid,
    primaryContainer = CyberCyanContainer,
    onPrimaryContainer = HudTextCyan,
    secondary = HologramGold,
    onSecondary = TitaniumVoid,
    secondaryContainer = HologramGoldContainer,
    onSecondaryContainer = HologramGold,
    tertiary = NeonEmerald,
    onTertiary = TitaniumVoid,
    background = TitaniumBackground,
    onBackground = HudTextPrimary,
    surface = TitaniumSurface,
    onSurface = HudTextPrimary,
    surfaceVariant = TitaniumSurfaceVariant,
    onSurfaceVariant = HudTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderGlow,
    error = NeonCrimson,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve the high-tech JARVIS HUD look
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
