package cz.zcu.kiv.dinh.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController

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
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Home") },
                    onClick = {
                        expanded = false
                        // Navigate to Camera screen
                        navController.navigate("camera") {
                            // Pop up to Help, but don't remove it (inclusive = false)
                            popUpTo("help") { inclusive = false }
                            // Avoid multiple instances of Camera in backstack
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
