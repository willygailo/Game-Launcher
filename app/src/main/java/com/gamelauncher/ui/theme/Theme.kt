package com.gamelauncher.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    tertiary = TertiaryAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    error = ErrorRed,
    onPrimary = BackgroundDark,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    tertiary = TertiaryAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    error = ErrorRed,
    onPrimary = BackgroundDark,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun GameLauncherTheme(
    darkTheme: Boolean = true,
    oledPureBlack: Boolean = true, // Default to true for the OLED pure black theme option
    content: @Composable () -> Unit
) {
    val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val colorScheme = if (oledPureBlack && darkTheme) {
        baseColorScheme.copy(
            background = OledBlack,
            surface = OledBlack,
            onPrimary = OledBlack
        )
    } else {
        baseColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = if (darkTheme) colorScheme.background.toArgb() else android.graphics.Color.WHITE
            @Suppress("DEPRECATION")
            window.navigationBarColor = if (darkTheme) colorScheme.background.toArgb() else android.graphics.Color.WHITE
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
