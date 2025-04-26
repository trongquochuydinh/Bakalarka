package cz.zcu.kiv.dinh.ui.results

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.navigation.NavController
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu
import cz.zcu.kiv.dinh.ui.components.BoundingBoxOverlay
import cz.zcu.kiv.dinh.ui.components.CoordTable

@Composable
fun TextRecognitionResultScreen(
    navController: NavController,
    imageUri: String,
    detectedResults: List<String>
) {
    val boxes = remember(detectedResults) {
        detectedResults.mapNotNull { result ->
            try {
                val boxString = result.substringAfter("(").substringBefore(")")
                    .replace("[", "").replace("]", "") // Odstranit hranaté závorky

                val coords = boxString.split(",").map { it.trim().toFloat() }
                if (coords.size == 4) {
                    ComposeRect(coords[0], coords[1], coords[2], coords[3])
                } else null
            } catch (e: Exception) {
                Log.e("TextRecognition", "Failed to parse box: $result", e)
                null
            }
        }
    }

    var selectedBoxIndex by remember { mutableStateOf<Int?>(null) }
    var showCoords by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Text Recognition Results") },
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
                text = if (cz.zcu.kiv.dinh.ml.configs.TextRecognitionConfig.useCloudModel)
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
