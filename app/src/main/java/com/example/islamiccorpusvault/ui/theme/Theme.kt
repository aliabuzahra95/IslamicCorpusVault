package com.example.islamiccorpusvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1F2937),          // Near-black ink
    onPrimary = Color.White,

    background = Color(0xFFF9FAFB),       // Soft paper
    onBackground = Color(0xFF111827),

    surface = Color.White,
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),

    outline = Color(0xFFE5E7EB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE5E7EB),           // Soft light ink
    onPrimary = Color(0xFF111827),

    background = Color(0xFF0B0F14),        // Deep night
    onBackground = Color(0xFFE5E7EB),

    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5E7EB),

    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),

    outline = Color(0xFF374151)
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