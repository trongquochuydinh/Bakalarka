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
import com.example.testing.ml.configs.ObjectDetectionConfig
import com.example.testing.ui.components.TopBarWithMenu

@Composable
fun ModelSettingsScreen(navController: androidx.navigation.NavController) {
    // For simplicity, we only implement text recognition settings.
    var mode by remember { mutableStateOf(TextRecognitionConfig.segmentationMode) }
    var highlightWord by remember { mutableStateOf(TextRecognitionConfig.highlightWord ?: "") }
    var highlightSymbol by remember { mutableStateOf(TextRecognitionConfig.highlightSymbol?.toString() ?: "") }
    var useCloudModelImageLabeling by remember { mutableStateOf(ImageLabelingConfig.useCloudModel) }
    var useCloudModelObjectDetection by remember { mutableStateOf(ObjectDetectionConfig.useCloudModel) }
    var useCloudModelTextRecognition by remember { mutableStateOf(TextRecognitionConfig.useCloudModel) }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Model Settings") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Text Recognition Settings", style = MaterialTheme.typography.titleMedium)
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

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use Cloud Model")
                Switch(
                    checked = useCloudModelTextRecognition,
                    onCheckedChange = {
                        useCloudModelTextRecognition = it
                        TextRecognitionConfig.useCloudModel = it
                    }
                )
            }

            // New Slider for Minimum Confidence Threshold
            Spacer(modifier = Modifier.height(16.dp))
            var sliderValue by remember { mutableFloatStateOf(ImageLabelingConfig.minConfidencePercentage.toFloat()) }
            Text("Image Labeling Settings", style = MaterialTheme.typography.titleMedium)
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

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use Cloud Model")
                Switch(
                    checked = useCloudModelImageLabeling,
                    onCheckedChange = {
                        useCloudModelImageLabeling = it
                        ImageLabelingConfig.useCloudModel = it
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(8.dp))
            Text("Object Detection Settings", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use Cloud Model")
                Switch(
                    checked = useCloudModelObjectDetection,
                    onCheckedChange = {
                        useCloudModelObjectDetection = it
                        ObjectDetectionConfig.useCloudModel = it
                    }
                )
            }
        }
    }
}
