package com.example.testing.ui.components

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
                        navController.popBackStack("camera", inclusive = false)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Visual Settings") },
                    onClick = {
                        expanded = false
                        navController.navigate("visual_settings") // Navigate to Visual Settings
                    }
                )
                DropdownMenuItem(
                    text = { Text("Model Settings") },
                    onClick = {
                        expanded = false
                        navController.navigate("model_settings") // Navigate to Model Settings
                    }
                )
            }
        }
    )
}
