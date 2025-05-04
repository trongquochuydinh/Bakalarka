package cz.zcu.kiv.dinh.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu

/**
 * Obrazovka pro vizuální nastavení aplikace.
 * Umožňuje přepínání mezi světlým a tmavým režimem pomocí komponenty ThemeSwitcher.
 *
 * @param navController Navigace zpět
 * @param isDarkTheme Aktuální režim (true = tmavý)
 * @param onThemeChange Funkce volaná při změně tématu
 */
@Composable
fun VisualSettingsScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Visual Settings") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.Start
        ) {
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Theme",
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )

            ThemeSwitcher(isDarkTheme, onThemeChange)
        }
    }
}

/**
 * Přepínač pro změnu vizuálního režimu (tmavý/světlý).
 * Používá ikony slunce a měsíce a vizuální zvýraznění vybraného režimu.
 *
 * @param isDarkTheme Aktuální režim
 * @param onThemeChange Funkce zavolaná při přepnutí tématu
 */
@Composable
fun ThemeSwitcher(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(24.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tlačítko pro aktivaci tmavého režimu
        IconButton(
            onClick = { onThemeChange(true) },
            modifier = Modifier
                .size(40.dp)
                .background(if (isDarkTheme) Color.DarkGray else Color.Transparent, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Dark Mode",
                tint = if (isDarkTheme) Color.White else Color.Black
            )
        }

        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "Switch",
            tint = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Tlačítko pro aktivaci světlého režimu
        IconButton(
            onClick = { onThemeChange(false) },
            modifier = Modifier
                .size(40.dp)
                .background(if (!isDarkTheme) Color.DarkGray else Color.Transparent, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.LightMode,
                contentDescription = "Light Mode",
                tint = if (!isDarkTheme) Color.White else Color.Black
            )
        }
    }
}
