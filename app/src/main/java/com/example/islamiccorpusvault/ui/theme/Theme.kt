package com.example.islamiccorpusvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Theme wiring (light/dark colorScheme).
 *
 * IMPORTANT:
 * - This file must pull colors from Color.kt
 * - Color.kt should only contain Color constants (tokens)
 */

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,

    background = Background,
    onBackground = OnSurface,

    surface = Surface,
    onSurface = OnSurface,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,

    background = Color(0xFF070D1A),
    onBackground = DarkOnSurface,

    surface = Color(0xFF0E1627),
    onSurface = DarkOnSurface,

    surfaceVariant = Color(0xFF1A2740),
    onSurfaceVariant = DarkOnSurfaceVariant,

    outline = DarkOutline
)

@Composable
fun IslamicCorpusVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
