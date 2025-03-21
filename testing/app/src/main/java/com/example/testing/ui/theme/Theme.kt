package com.example.testing.ui.theme

import android.app.Activity
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
    primary = BackgroundAppDarkThemeColor,
    secondary = BackgroundButtonDarkThemeColor,
    tertiary = IconDarkThemeColor
)

private val LightColorScheme = lightColorScheme(
    primary = BackgroundAppLightThemeColor,
    secondary = BackgroundButtonLightThemeColor,
    tertiary = IconDarkLightColor

)

@Composable
fun TestingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Use system setting or pass manually
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}