package com.example.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = LabPrimaryTeal,
    onPrimary = Color.White,
    primaryContainer = LabDarkTeal,
    onPrimaryContainer = Color.White,
    secondary = LabPrimaryTeal,
    onSecondary = Color.White,
    tertiary = LabRed,
    onTertiary = Color.White,
    background = SurfaceBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = LabTealSurface,
    onSurfaceVariant = TextPrimary,
    outline = BorderSubtle
)

private val DarkColorScheme = darkColorScheme(
    primary = LabTealAccent,
    onPrimary = Color(0xFF003733),
    primaryContainer = LabDarkTeal,
    onPrimaryContainer = LabTealLight,
    secondary = LabTealAccent,
    onSecondary = Color(0xFF003733),
    tertiary = Color(0xFFFF7B6B),
    onTertiary = Color.White,
    background = Color(0xFF14171A),
    onBackground = Color(0xFFECEFF2),
    surface = Color(0xFF1E2328),
    onSurface = Color(0xFFECEFF2),
    surfaceVariant = Color(0xFF262C33),
    onSurfaceVariant = Color(0xFFECEFF2),
    outline = Color(0xFF2F363D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
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
