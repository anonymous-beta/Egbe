package com.egbe.surveillance.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EGBEGreen,
    secondary = EGBECyan,
    tertiary = EGBEAmber,
    background = EGBEBlack,
    surface = EGBESurface,
    onPrimary = EGBEText,
    onSecondary = EGBEText,
    onBackground = EGBEText,
    onSurface = EGBEText,
    error = EGBERed
)

@Composable
fun EGBETheme(
    darkTheme: Boolean = true, // EGBE is always dark
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EGBETypography,
        content = content
    )
}
