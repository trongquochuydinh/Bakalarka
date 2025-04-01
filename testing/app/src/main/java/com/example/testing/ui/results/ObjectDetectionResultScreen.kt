// ObjectDetectionResultScreen.kt
package com.example.testing.ui.results

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import com.example.testing.ui.components.TopBarWithMenu

// TODO: Display a table containing the ID of a bounding box and its corresponding coordinates
// TODO: Each bounding box should have an ID displayed
// TODO: Make the drawing of a bounding box using coordinates a component which will be shared in ObjectDetectionResultScreen and TextRecognitionResultScreen

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetectionResultScreen(
    navController: NavController,
    imageUri: String,
    detectionResults: List<String>
) {
    // Parse each result string into a data class for easier handling.
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

    // This variable will hold the drawn image size
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current

    // Use Coil's AsyncImagePainter with additional metadata to get original dimensions
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .size(CoilSize.ORIGINAL) // Request original size to get metadata
            .allowHardware(false) // Disable hardware bitmaps for direct access
            .build()
    )
    val painterState = painter.state

    // Track original image dimensions
    var originalWidth by remember { mutableStateOf(0f) }
    var originalHeight by remember { mutableStateOf(0f) }

    // Update original dimensions when image is loaded
    LaunchedEffect(painterState) {
        if (painterState is AsyncImagePainter.State.Success) {
            val drawable = painterState.result.drawable
            originalWidth = drawable.intrinsicWidth.toFloat()
            originalHeight = drawable.intrinsicHeight.toFloat()
            Log.d("ObjectDetection", "Original image dimensions: $originalWidth x $originalHeight")
        }
    }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Object Detection Results") },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display the image and capture its size
            Image(
                painter = painter,
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        imageSize = size
                        Log.d("ObjectDetection", "Image display size: $size")
                    },
                contentScale = ContentScale.Fit
            )

            // Overlay canvas for bounding boxes
            if (painterState is AsyncImagePainter.State.Success &&
                imageSize != IntSize.Zero &&
                originalWidth > 0 &&
                originalHeight > 0) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    Log.d("ObjectDetection", "Original image size: $originalWidth x $originalHeight")
                    Log.d("ObjectDetection", "Canvas size: ${size.width} x ${size.height}")
                    Log.d("ObjectDetection", "Image display size: ${imageSize.width} x ${imageSize.height}")

                    // Calculate scale factor for ContentScale.Fit
                    val widthRatio = imageSize.width / originalWidth
                    val heightRatio = imageSize.height / originalHeight
                    val scaleFactor = minOf(widthRatio, heightRatio)

                    // Calculate offset for letterboxing (centering the image)
                    val offsetX = (imageSize.width - originalWidth * scaleFactor) / 2f
                    val offsetY = (imageSize.height - originalHeight * scaleFactor) / 2f

                    Log.d("ObjectDetection", "Scale factor: $scaleFactor, Offset: ($offsetX, $offsetY)")

                    // Draw each bounding box scaled appropriately
                    detections.forEach { detection ->
                        val scaledLeft = detection.box.left * scaleFactor + offsetX
                        val scaledTop = detection.box.top * scaleFactor + offsetY
                        val scaledWidth = (detection.box.right - detection.box.left) * scaleFactor
                        val scaledHeight = (detection.box.bottom - detection.box.top) * scaleFactor

                        Log.d("ObjectDetection", "Drawing box: ${detection.label} at $scaledLeft,$scaledTop with size $scaledWidth x $scaledHeight")

                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(scaledLeft, scaledTop),
                            size = Size(scaledWidth, scaledHeight),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // In a real implementation, you might want to draw the label text here
                    }
                }
            }
        }
    }
}