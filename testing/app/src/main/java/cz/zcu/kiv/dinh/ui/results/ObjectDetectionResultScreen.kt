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

@Composable
fun ObjectDetectionResultScreen(
    navController: NavController,
    imageUri: String,
    detectionResults: List<String>
) {
    data class Detection(val label: String, val box: ComposeRect)

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

    val boxes = detections.map { it.box }
    var selectedBoxIndex by remember { mutableStateOf<Int?>(null) }
    var showCoords by remember { mutableStateOf(true) }


    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Object Detection Results") },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BoundingBoxOverlay(
                imageUri = imageUri,
                boundingBoxes = boxes,
                selectedIndex = selectedBoxIndex,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (cz.zcu.kiv.dinh.ml.configs.ObjectDetectionConfig.useCloudModel)
                    "Model: Cloud" else "Model: On-Device",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showCoords = !showCoords },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(if (showCoords) "Hide Coordinates" else "Show Coordinates")
            }

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
