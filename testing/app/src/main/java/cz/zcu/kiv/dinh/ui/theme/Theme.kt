package cz.zcu.kiv.dinh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Definice barevného schématu pro tmavý režim
private val DarkColorScheme = darkColorScheme(
    primary = BackgroundAppDarkThemeColor,
    secondary = BackgroundButtonDarkThemeColor,
    tertiary = IconDarkThemeColor
)

// Definice barevného schématu pro světlý režim
private val LightColorScheme = lightColorScheme(
    primary = BackgroundAppLightThemeColor,
    secondary = BackgroundButtonLightThemeColor,
    tertiary = IconDarkLightColor
)

/**
 * Aplikační téma, které se dynamicky přizpůsobuje světlému nebo tmavému režimu.
 *
 * @param darkTheme Určuje, zda má být použit tmavý režim. Pokud není nastaveno, použije se systémové nastavení.
 * @param content Obsah, na který se má aplikovat téma.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Použije výchozí systémové nastavení
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Aplikace vybraného barevného schématu a typografie
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}