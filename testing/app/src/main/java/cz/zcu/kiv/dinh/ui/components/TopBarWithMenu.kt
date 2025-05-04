package cz.zcu.kiv.dinh.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController

/**
 * Vrchní panel s ikonou menu, které umožňuje navigaci mezi hlavními obrazovkami aplikace.
 *
 * @param navController Navigační kontroler Jetpack Navigation komponenty.
 * @param title Titulek, který se zobrazí ve vrchním panelu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithMenu(navController: NavController, title: String) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            // Dropdown menu s položkami pro navigaci
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Home") },
                    onClick = {
                        expanded = false
                        navController.navigate("camera") {
                            popUpTo("help") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Visual Settings") },
                    onClick = {
                        expanded = false
                        navController.navigate("visual_settings") {
                            launchSingleTop = true
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Model Settings") },
                    onClick = {
                        expanded = false
                        navController.navigate("model_settings") {
                            launchSingleTop = true
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Help") },
                    onClick = {
                        expanded = false
                        navController.navigate("help") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    )
}