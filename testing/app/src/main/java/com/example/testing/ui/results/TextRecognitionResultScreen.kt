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
                // Expecting: "Block: Some text ([left, top, right, bottom])"
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

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Text Recognition Results") },
    ) { paddingValues ->
        BoundingBoxOverlay(
            imageUri = imageUri,
            boundingBoxes = boxes,
            modifier = Modifier
                .padding(paddingValues)
        )
    }
}
