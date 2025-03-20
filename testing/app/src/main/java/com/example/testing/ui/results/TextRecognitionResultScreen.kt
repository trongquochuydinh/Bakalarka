// TextRecognitionResultScreen.kt
package com.example.testing.ui.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testing.ui.components.TopBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextRecognitionResultScreen(
    navController: NavController,
    imageUri: String,
    detectedResults: List<String>
) {
    // Parse bounding boxes from result strings.
    val boxes = remember(detectedResults) {
        detectedResults.mapNotNull { result ->
            try {
                // Expecting a result string like: "Block: Some text ([left, top, right, bottom])"
                val boxString = result.substringAfter("([").substringBefore("])")
                val coords = boxString.split(",").map { it.trim().toFloat() }
                if (coords.size == 4) ComposeRect(coords[0], coords[1], coords[2], coords[3])
                else null
            } catch (e: Exception) {
                null
            }
        }
    }

    // State to capture the size of the displayed image
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    // Load image with Coil
    val painter = rememberAsyncImagePainter(model = imageUri)

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Text Recognition Results") },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display the image and capture its size when drawn
            Image(
                painter = painter,
                contentDescription = "Original Image",
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        imageSize = size
                    },
                contentScale = ContentScale.Fit
            )
            // Overlay the bounding boxes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val intrinsicSize = painter.intrinsicSize
                // Guard against unspecified intrinsic size
                if (intrinsicSize == androidx.compose.ui.geometry.Size.Unspecified ||
                    intrinsicSize.width <= 0f ||
                    intrinsicSize.height <= 0f ||
                    imageSize.width <= 0 ||
                    imageSize.height <= 0) {
                    // Either return early or draw without scaling if appropriate.
                    return@Canvas
                }

                // Calculate scale factor for ContentScale.Fit
                val scaleFactor = minOf(
                    imageSize.width / intrinsicSize.width,
                    imageSize.height / intrinsicSize.height
                )
                // Calculate offsets for letterboxing if any
                val dx = (imageSize.width - intrinsicSize.width * scaleFactor) / 2f
                val dy = (imageSize.height - intrinsicSize.height * scaleFactor) / 2f

                boxes.forEach { box ->
                    // Scale and offset each bounding box coordinate
                    val scaledLeft = box.left * scaleFactor + dx
                    val scaledTop = box.top * scaleFactor + dy
                    val scaledRight = box.right * scaleFactor + dx
                    val scaledBottom = box.bottom * scaleFactor + dy

                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(scaledLeft, scaledTop),
                        size = androidx.compose.ui.geometry.Size(scaledRight - scaledLeft, scaledBottom - scaledTop),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
        }
    }
}
