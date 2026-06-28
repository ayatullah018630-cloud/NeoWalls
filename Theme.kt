package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeoPrimaryDark,
    secondary = NeoSecondaryDark,
    tertiary = NeoTertiaryDark,
    background = NeoBackgroundDark,
    surface = NeoSurfaceDark,
    onPrimary = NeoBackgroundDark,
    onSecondary = NeoBackgroundDark,
    onTertiary = NeoBackgroundDark,
    onBackground = NeoOnBackgroundDark,
    onSurface = NeoOnBackgroundDark,
    surfaceVariant = CardSlate
)

private val LightColorScheme = lightColorScheme(
    primary = NeoPrimaryLight,
    secondary = NeoSecondaryLight,
    tertiary = NeoTertiaryLight,
    background = NeoBackgroundLight,
    surface = NeoSurfaceLight,
    onPrimary = NeoSurfaceLight,
    onSecondary = NeoSurfaceLight,
    onTertiary = NeoSurfaceLight,
    onBackground = NeoOnBackgroundLight,
    onSurface = NeoOnBackgroundLight,
    surfaceVariant = NeoSurfaceLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Keep false by default to showcase our custom neo-brand colors!
    content: @Composable () -> Unit,
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
