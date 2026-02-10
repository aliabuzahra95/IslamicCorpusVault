package com.example.islamiccorpusvault.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ---- Brand palette (light) ----
private val IcvPrimary = Color(0xFF2E5E4E)        // deep green (not neon)
private val IcvOnPrimary = Color(0xFFFFFFFF)

private val IcvBackground = Color(0xFFF6F4EF)     // warm paper
private val IcvSurface = Color(0xFFFDFBF7)        // slightly brighter paper
private val IcvSurfaceVariant = Color(0xFFECE7DE) // soft card background
private val IcvOnSurface = Color(0xFF1E1B16)
private val IcvOnSurfaceVariant = Color(0xFF4B463F)
private val IcvOutline = Color(0xFFCEC7BC)

// ---- Brand palette (dark) ----
private val IcvDarkPrimary = Color(0xFF89C7B2)
private val IcvDarkOnPrimary = Color(0xFF0B2019)

private val IcvDarkBackground = Color(0xFF0F1412)
private val IcvDarkSurface = Color(0xFF131A17)
private val IcvDarkSurfaceVariant = Color(0xFF1A2420)
private val IcvDarkOnSurface = Color(0xFFE7E1D8)
private val IcvDarkOnSurfaceVariant = Color(0xFFB7B1A8)
private val IcvDarkOutline = Color(0xFF3A4A43)

private val DarkColorScheme = darkColorScheme(
    primary = IcvDarkPrimary,
    onPrimary = IcvDarkOnPrimary,
    background = IcvDarkBackground,
    surface = IcvDarkSurface,
    surfaceVariant = IcvDarkSurfaceVariant,
    onSurface = IcvDarkOnSurface,
    onSurfaceVariant = IcvDarkOnSurfaceVariant,
    outline = IcvDarkOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = IcvPrimary,
    onPrimary = IcvOnPrimary,
    background = IcvBackground,
    surface = IcvSurface,
    surfaceVariant = IcvSurfaceVariant,
    onSurface = IcvOnSurface,
    onSurfaceVariant = IcvOnSurfaceVariant,
    outline = IcvOutline,
)

@Composable
fun IslamicCorpusVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep this false so your app looks the same on every phone (no random dynamic colors).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}