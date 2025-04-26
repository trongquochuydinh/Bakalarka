// ImageLabelingResultScreen.kt
package cz.zcu.kiv.dinh.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu

data class LabelItem(val text: String, val confidence: Int)

@Composable
fun ImageLabelingResultScreen(
    navController: NavController,
    imageUri: String,
    detectedLabels: List<String>
) {
    var sortAscending by remember { mutableStateOf(false) }
    var showImage by remember { mutableStateOf(false) }

    val labelItems = remember(detectedLabels) {
        detectedLabels.map { label ->
            // More robust parsing logic that handles different label formats
            val parts = label.split("(", ")")
            val labelText = parts[0].trim()
            val confidenceText = parts.getOrElse(1) { "0%" }.replace("%", "").trim()
            val confidence = confidenceText.toIntOrNull() ?: 0
            LabelItem(labelText, confidence)
        }
    }

    // Remember sorted items to prevent unnecessary resorting
    val sortedItems = remember(labelItems, sortAscending) {
        labelItems.sortedWith(compareBy {
            if (sortAscending) it.confidence else -it.confidence
        })
    }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Image Labeling Results") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF625A5A))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Toggle + Sort Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { showImage = !showImage }) {
                    Text(if (showImage) "Hide Image" else "Show Image")
                }

                Button(onClick = { sortAscending = !sortAscending }) {
                    Text(if (sortAscending) "Sort: Ascending" else "Sort: Descending")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (cz.zcu.kiv.dinh.ml.configs.ImageLabelingConfig.useCloudModel)
                    "Model: Cloud" else "Model: On-Device",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )

            // Show Image if toggled
            if (showImage) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Original Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Label",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Confidence",
                    modifier = Modifier.weight(0.5f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = Color.LightGray)

            // Table Rows
            if (sortedItems.isEmpty()) {
                Text(
                    text = "No labels detected.",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = sortedItems,
                        key = { "${it.text}_${it.confidence}" }
                    ) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.text,
                                    modifier = Modifier.weight(1f),
                                    color = Color.Black
                                )
                                Text(
                                    text = "${item.confidence}%",
                                    modifier = Modifier.weight(0.5f),
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
