// ModelSettingsScreen.kt
package com.example.testing.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testing.ml.TextRecognitionConfig
import com.example.testing.ui.components.TopBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSettingsScreen(navController: androidx.navigation.NavController) {
    // For simplicity, we only implement text recognition settings.
    var mode by remember { mutableStateOf(TextRecognitionConfig.segmentationMode) }
    var highlightWord by remember { mutableStateOf(TextRecognitionConfig.highlightWord ?: "") }
    var highlightSymbol by remember { mutableStateOf(TextRecognitionConfig.highlightSymbol?.toString() ?: "") }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Model Settings") },
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {
            Text(text = "Text Recognition Segmentation Mode")
            // Dropdown to select segmentation mode.
            var expanded by remember { mutableStateOf(false) }
            Box {
                Text(
                    text = mode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("block", "line", "word", "symbol").forEach { option ->
                        DropdownMenuItem(text = { Text(option) },
                            onClick = {
                                mode = option
                                expanded = false
                                TextRecognitionConfig.segmentationMode = option
                            })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (mode == "word") {
                OutlinedTextField(
                    value = highlightWord,
                    onValueChange = {
                        highlightWord = it
                        TextRecognitionConfig.highlightWord = if (it.isBlank()) null else it
                    },
                    label = { Text("Highlight Word (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (mode == "symbol") {
                OutlinedTextField(
                    value = highlightSymbol,
                    onValueChange = {
                        highlightSymbol = it
                        TextRecognitionConfig.highlightSymbol = if (it.isNotEmpty()) it[0] else null
                    },
                    label = { Text("Highlight Symbol (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
