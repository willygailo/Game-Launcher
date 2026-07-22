// app/src/main/java/com/gamelauncher/ui/theme/Color.kt
package com.gamelauncher.ui.theme

import androidx.compose.ui.graphics.Color

// Game Launcher Pro — Original Cyber Neon Theme Colors
val BackgroundDark = Color(0xFF09090B)       // Dark charcoal surface
val OledBlack = Color(0xFF000000)            // Pure Black for OLED displays
val SurfaceDark = Color(0xFF14171F)          // Deep obsidian container surface
val SurfaceVariantDark = Color(0xFF1F2433)   // Card border / secondary surface

// Neon Accents (Original Cyberpunk Palette - Cyan/Purple/Pink)
val PrimaryNeon = Color(0xFF00E5FF)          // Cyber Cyan Primary Accent
val SecondaryNeon = Color(0xFFB900FF)        // Neon Violet Secondary Accent
val TertiaryAccent = Color(0xFFFF0055)       // Electric Crimson Accent

val SuccessGreen = Color(0xFF00FF66)         // Matrix Emerald
val WarningOrange = Color(0xFFFF9900)        // Neon Amber
val ErrorRed = Color(0xFFFF3333)             // Signal Red

val TextPrimary = Color(0xFFFAFAFA)
val TextSecondary = Color(0xFFA1A1AA)

// Game Dock Panel Color Tokens
val DockBgDark = Color(0xFF0C0F17)
val DockSurfaceDark = Color(0xFF161B26)
val DockCardBorder = Color(0xFF252D3F)

enum class PerformanceMode {
    ECO, BALANCED, PRO
}

fun getModeColor(mode: PerformanceMode): Color = when(mode) {
    PerformanceMode.ECO -> SuccessGreen
    PerformanceMode.BALANCED -> PrimaryNeon
    PerformanceMode.PRO -> TertiaryAccent
}
