package com.example.testing.ui.results

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.navigation.NavController
import com.example.testing.ui.components.TopBarWithMenu
import com.example.testing.ui.components.BoundingBoxOverlay
import com.example.testing.ui.components.CoordTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextRecognitionResultScreen(
    navController: NavController,
    imageUri: String,
    detectedResults: List<String>
) {
    val boxes = remember(detectedResults) {
        detectedResults.mapNotNull { result ->
            try {
                val boxString = result.substringAfter("([").substringBefore("])")
                val coords = boxString.split(",").map { it.trim().toFloat() }
                if (coords.size == 4) ComposeRect(coords[0], coords[1], coords[2], coords[3])
                else null
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
        Column(modifier = Modifier.padding(paddingValues)) {
            BoundingBoxOverlay(
                imageUri = imageUri,
                boundingBoxes = boxes,
                selectedIndex = selectedBoxIndex,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

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
