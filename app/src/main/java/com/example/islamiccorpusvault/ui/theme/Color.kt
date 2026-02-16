package com.example.islamiccorpusvault.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens used by Theme.kt.
 *
 * Rule:
 * - Color.kt ONLY contains Color constants.
 * - Theme.kt is the only place that builds the Material colorScheme.
 *
 * Style target (D):
 * Clean + dimensional (calm background, crisp surfaces, subtle contrast).
 */

// ---- Light ----
val Primary = Color(0xFF2F6BFF)          // blue accent
val OnPrimary = Color(0xFFFFFFFF)

// Slightly tinted background so surfaces/cards pop (depth)
val Background = Color(0xFFF6F7FB)

// Base surfaces
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFEFF2F8)

// Text colors
val OnSurface = Color(0xFF0F172A)
val OnSurfaceVariant = Color(0xFF475569)

// Borders / dividers
val Outline = Color(0xFFCBD5E1)


// ---- Dark ----
val DarkPrimary = Color(0xFF7AA2FF)     // softer blue for dark mode
val DarkOnPrimary = Color(0xFF0B1220)

val DarkBackground = Color(0xFF0B1020)
val DarkSurface = Color(0xFF0F172A)
val DarkSurfaceVariant = Color(0xFF162033)

val DarkOnSurface = Color(0xFFE5E7EB)
val DarkOnSurfaceVariant = Color(0xFF9AA6B2)

val DarkOutline = Color(0xFF2A3650)