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

/**
 * Datová třída pro položku detekovaného štítku (label).
 *
 * @property text Název detekovaného objektu/štítku
 * @property confidence Pravděpodobnost (v %) s jakou byl štítek rozpoznán
 */
data class LabelItem(val text: String, val confidence: Int)

/**
 * Obrazovka pro zobrazení výsledků označování obrázků.
 * Umožňuje zobrazení seznamu detekovaných štítků, jejich třídění podle pravděpodobnosti a zobrazení původního obrázku.
 *
 * @param navController Navigace zpět
 * @param imageUri URI obrázku, který byl analyzován
 * @param detectedLabels Výsledky detekce ve formátu "label (confidence%)"
 * @param processingTime Doba zpracování v milisekundách
 */
@Composable
fun ImageLabelingResultScreen(
    navController: NavController,
    imageUri: String,
    detectedLabels: List<String>,
    processingTime: Float
) {
    var sortAscending by remember { mutableStateOf(false) } // Řazení dle pravděpodobnosti
    var showImage by remember { mutableStateOf(false) } // Zobrazení/skrytí původního obrázku

    // Převedení vstupních řetězců na objekty LabelItem
    val labelItems = remember(detectedLabels) {
        detectedLabels.map { label ->
            val parts = label.split("(", ")")
            val labelText = parts[0].trim()
            val confidenceText = parts.getOrElse(1) { "0%" }.replace("%", "").trim()
            val confidence = confidenceText.toIntOrNull() ?: 0
            LabelItem(labelText, confidence)
        }
    }

    // Řazení seznamu dle vybraného směru
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
            // Tlačítka pro zobrazení obrázku a řazení
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

            // Zobrazení typu použitého modelu
            Text(
                text = if (cz.zcu.kiv.dinh.ml.configs.ImageLabelingConfig.useCloudModel)
                    "Model: Cloud" else "Model: On-Device",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )

            // Zobrazení doby zpracování
            Text(
                text = "Processing Time: ${processingTime} ms",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Zobrazení původního obrázku (pokud je aktivní)
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

            // Hlavička tabulky
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

            // Zobrazení detekovaných štítků
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