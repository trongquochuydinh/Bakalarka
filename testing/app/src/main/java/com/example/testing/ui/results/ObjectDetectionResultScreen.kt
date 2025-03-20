// ObjectDetectionResultScreen.kt
package com.example.testing.ui.results

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.testing.ui.components.TopBarWithMenu


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
                null
            }
        }
    }

    // This variable will hold the drawn image size.
    var drawnImageSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    // Use Coil's AsyncImagePainter to load the image.
    val painter = rememberAsyncImagePainter(model = imageUri)

    Scaffold(
        topBar = { TopBarWithMenu(navController, title = "Object Detection Results") },
    ) { paddingValues ->
        // BoxWithConstraints lets you get maxWidth easily.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display the image and capture its drawn size.
            Image(
                painter = painter,
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { drawnImageSize = it },
                contentScale = ContentScale.FillWidth
            )
            // Overlay a Canvas on top of the image.
            Canvas(modifier = Modifier.fillMaxWidth().height(with(androidx.compose.ui.platform.LocalDensity.current) { drawnImageSize.height.toDp() })) {
                // Only proceed if we have the original image dimensions.
                if (painter.state is coil.compose.AsyncImagePainter.State.Success) {
                    val drawable = (painter.state as coil.compose.AsyncImagePainter.State.Success).result.drawable
                    val originalWidth = drawable.intrinsicWidth.toFloat()
                    val originalHeight = drawable.intrinsicHeight.toFloat()
                    // Our image is displayed with ContentScale.FillWidth.
                    // So the displayed width equals the canvas width.
                    val scaleX = size.width / originalWidth
                    // The displayed height will be originalHeight * scaleX.
                    val scaleY = size.height / originalHeight

                    // Draw each bounding box scaled appropriately.
                    detections.forEach { detection ->
                        val scaledLeft = detection.box.left * scaleX
                        val scaledTop = detection.box.top * scaleY
                        val scaledWidth = detection.box.width * scaleX
                        val scaledHeight = detection.box.height * scaleY
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(scaledLeft, scaledTop),
                            size = androidx.compose.ui.geometry.Size(scaledWidth, scaledHeight),
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )
                        // Optionally, you can also draw the label text.
                    }
                }
            }
        }
    }
}
