// ModelSettingsScreen.kt
package com.example.testing.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testing.ml.configs.TextRecognitionConfig
import com.example.testing.ml.configs.ImageLabelingConfig
import com.example.testing.ui.components.TopBarWithMenu

@Composable
fun ModelSettingsScreen(navController: androidx.navigation.NavController) {
    // For simplicity, we only implement text recognition settings.
    var mode by remember { mutableStateOf(TextRecognitionConfig.segmentationMode) }
    var highlightWord by remember { mutableStateOf(TextRecognitionConfig.highlightWord ?: "") }
    var highlightSymbol by remember { mutableStateOf(TextRecognitionConfig.highlightSymbol?.toString() ?: "") }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Model Settings") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                mode = option
                                expanded = false
                                TextRecognitionConfig.segmentationMode = option
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (mode == "word") {
                OutlinedTextField(
                    value = highlightWord,
                    onValueChange = {
                        highlightWord = it
                        TextRecognitionConfig.highlightWord = it.ifBlank { null }
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
            // New Slider for Minimum Confidence Threshold
            Spacer(modifier = Modifier.height(16.dp))
            var sliderValue by remember { mutableFloatStateOf(ImageLabelingConfig.minConfidencePercentage.toFloat()) }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Minimum Confidence for Image Labeling: ${sliderValue.toInt()}%")
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    ImageLabelingConfig.minConfidencePercentage = newValue.toInt()
                },
                valueRange = 0f..100f,
                steps = 99, // each step represents 1%
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
