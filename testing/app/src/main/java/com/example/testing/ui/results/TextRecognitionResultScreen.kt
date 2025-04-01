// TextRecognitionResultScreen.kt
package com.example.testing.ui.results

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size as CoilSize
import com.example.testing.ui.components.TopBarWithMenu

@SuppressLint("UnusedBoxWithConstraintsScope")
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
                Log.e("TextRecognition", "Failed to parse box: $result", e)
                null
            }
        }
    }

    // State to capture the size of the displayed image
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current

    // Load image with Coil and get metadata
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
            Log.d("TextRecognition", "Original image dimensions: $originalWidth x $originalHeight")
        }
    }

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Text Recognition Results") },
    ) { paddingValues ->
        BoxWithConstraints(
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
                        Log.d("TextRecognition", "Image display size: $size")
                    },
                contentScale = ContentScale.Fit
            )

            // Only draw overlays when the image is successfully loaded and dimensions are known
            if (painterState is AsyncImagePainter.State.Success &&
                imageSize != IntSize.Zero &&
                originalWidth > 0 &&
                originalHeight > 0) {

                Canvas(modifier = Modifier.fillMaxSize()) {
                    Log.d("TextRecognition", "Original image size: $originalWidth x $originalHeight")
                    Log.d("TextRecognition", "Canvas size: ${size.width} x ${size.height}")
                    Log.d("TextRecognition", "Image display size: ${imageSize.width} x ${imageSize.height}")

                    // Calculate scale factor for ContentScale.Fit
                    val widthRatio = imageSize.width / originalWidth
                    val heightRatio = imageSize.height / originalHeight
                    val scaleFactor = minOf(widthRatio, heightRatio)

                    // Calculate offsets for letterboxing (centering)
                    val offsetX = (imageSize.width - originalWidth * scaleFactor) / 2f
                    val offsetY = (imageSize.height - originalHeight * scaleFactor) / 2f

                    Log.d("TextRecognition", "Scale factor: $scaleFactor, Offset: ($offsetX, $offsetY)")

                    boxes.forEach { box ->
                        // Scale coordinates and dimensions instead of assuming the box is a rectangle
                        val scaledLeft = box.left * scaleFactor + offsetX
                        val scaledTop = box.top * scaleFactor + offsetY
                        val scaledWidth = (box.right - box.left) * scaleFactor
                        val scaledHeight = (box.bottom - box.top) * scaleFactor

                        Log.d("TextRecognition", "Drawing box at $scaledLeft,$scaledTop with size $scaledWidth x $scaledHeight")

                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(scaledLeft, scaledTop),
                            size = Size(scaledWidth, scaledHeight),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}