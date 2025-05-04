package cz.zcu.kiv.dinh.ui.results

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.navigation.NavController
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu
import cz.zcu.kiv.dinh.ui.components.BoundingBoxOverlay
import cz.zcu.kiv.dinh.ui.components.CoordTable
import androidx.compose.ui.Alignment

/**
 * Obrazovka pro zobrazení výsledků detekce objektů.
 * Zobrazuje náhled obrázku s překryvnými ohraničujícími boxy a tabulkou souřadnic.
 *
 * @param navController Navigace zpět
 * @param imageUri URI obrázku, na kterém byla detekce provedena
 * @param detectionResults Výsledky detekce jako seznam textových řetězců (label + bounding box)
 * @param processingTime Čas zpracování v milisekundách
 */
@Composable
fun ObjectDetectionResultScreen(
    navController: NavController,
    imageUri: String,
    detectionResults: List<String>,
    processingTime: Float
) {
    // Pomocná datová třída pro zpracované detekce
    data class Detection(val label: String, val box: ComposeRect)

    // Parsování výsledků detekce na objekty Detection
    val detections = remember(detectionResults) {
        detectionResults.mapNotNull { result ->
            try {
                val parts = result.split(" - Box: ")
                val label = parts[0]
                val boxValues = parts[1].removePrefix("[").removeSuffix("]").split(", ")
                val left = boxValues[0].toFloat()
                val top = boxValues[1].toFloat()
                val right = boxValues[2].toFloat()
                val bottom = boxValues[3].toFloat()
                Detection(label, ComposeRect(left, top, right, bottom))
            } catch (e: Exception) {
                Log.e("ObjectDetection", "Failed to parse detection: $result", e)
                null
            }
        }
    }

    val boxes = detections.map { it.box } // Extrakce pouze ohraničujících boxů
    var selectedBoxIndex by remember { mutableStateOf<Int?>(null) } // Index vybraného boxu
    var showCoords by remember { mutableStateOf(true) } // Zobrazení/skrytí souřadnic

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Object Detection Results") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Komponenta pro zobrazení obrázku s překryvnými boxy
            BoundingBoxOverlay(
                imageUri = imageUri,
                boundingBoxes = boxes,
                selectedIndex = selectedBoxIndex,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Informace o typu použitého modelu (cloud vs. offline)
            Text(
                text = if (cz.zcu.kiv.dinh.ml.configs.ObjectDetectionConfig.useCloudModel)
                    "Model: Cloud" else "Model: On-Device",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )

            // Zobrazení času zpracování
            Text(
                text = "Processing Time: ${processingTime} ms",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tlačítko pro přepínání zobrazení tabulky souřadnic
            Button(
                onClick = { showCoords = !showCoords },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (showCoords) "Hide Coordinates" else "Show Coordinates")
            }

            // Zobrazení tabulky souřadnic, pokud je aktivní
            if (showCoords) {
                CoordTable(
                    boundingBoxes = boxes,
                    selectedIndex = selectedBoxIndex,
                    onSelect = { selectedBoxIndex = it }
                )
            }
        }
    }
}