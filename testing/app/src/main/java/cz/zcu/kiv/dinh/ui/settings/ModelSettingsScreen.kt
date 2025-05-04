// ModelSettingsScreen.kt
package cz.zcu.kiv.dinh.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cz.zcu.kiv.dinh.ml.configs.TextRecognitionConfig
import cz.zcu.kiv.dinh.ml.configs.ImageLabelingConfig
import cz.zcu.kiv.dinh.ml.configs.ObjectDetectionConfig
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu

/**
 * Obrazovka pro nastavení modelů strojového učení.
 * Uživatel zde může zvolit segmentaci textu, zvýraznit konkrétní slova/symboly
 * a zapnout Cloud modely nebo nastavit minimální prahy důvěry pro Image Labeling a Object Detection.
 *
 * @param navController Navigace zpět
 */
@Composable
fun ModelSettingsScreen(navController: androidx.navigation.NavController) {
    // Text Recognition: režim segmentace a zvýraznění
    var mode by remember { mutableStateOf(TextRecognitionConfig.segmentationMode) }
    var highlightWord by remember { mutableStateOf(TextRecognitionConfig.highlightWord ?: "") }
    var highlightSymbol by remember { mutableStateOf(TextRecognitionConfig.highlightSymbol?.toString() ?: "") }

    // Cloud modely (zapnuto/vypnuto)
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
            // --- TEXT RECOGNITION ---
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Text Recognition Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Výběr režimu segmentace
            Text("Segmentation Mode")
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

            // Textová pole pro zvýraznění (slovo / symbol)
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

            // Přepínač cloud modelu pro Text Recognition
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Use Cloud Model")
                Switch(
                    checked = useCloudModelTextRecognition,
                    onCheckedChange = {
                        useCloudModelTextRecognition = it
                        TextRecognitionConfig.useCloudModel = it
                    }
                )
            }

            // --- IMAGE LABELING ---
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Image Labeling Settings", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Slider pro minimální důvěru
            var sliderImageLabelingValue by remember { mutableFloatStateOf(ImageLabelingConfig.minConfidencePercentage.toFloat()) }
            Text("Minimum Confidence: ${sliderImageLabelingValue.toInt()}%")
            Slider(
                value = sliderImageLabelingValue,
                onValueChange = {
                    sliderImageLabelingValue = it
                    ImageLabelingConfig.minConfidencePercentage = it.toInt()
                },
                valueRange = 0f..100f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )

            // Přepínač cloud modelu
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Use Cloud Model")
                Switch(
                    checked = useCloudModelImageLabeling,
                    onCheckedChange = {
                        useCloudModelImageLabeling = it
                        ImageLabelingConfig.useCloudModel = it
                    }
                )
            }

            // --- OBJECT DETECTION ---
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Object Detection Settings", style = MaterialTheme.typography.titleMedium)

            var sliderObjectDetectionValue by remember { mutableFloatStateOf(ObjectDetectionConfig.minConfidencePercentage.toFloat()) }
            Text("Minimum Confidence: ${sliderObjectDetectionValue.toInt()}%")
            Slider(
                value = sliderObjectDetectionValue,
                onValueChange = {
                    sliderObjectDetectionValue = it
                    ObjectDetectionConfig.minConfidencePercentage = it.toInt()
                },
                valueRange = 0f..100f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
