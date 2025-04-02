package com.example.testing.ui.results

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.navigation.NavController
import com.example.testing.ui.components.TopBarWithMenu
import com.example.testing.ui.components.BoundingBoxOverlay

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

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Object Detection Results") },
    ) { paddingValues ->
        BoundingBoxOverlay(
            imageUri = imageUri,
            boundingBoxes = boxes,
            modifier = Modifier
                .padding(paddingValues)
        )
    }
}
