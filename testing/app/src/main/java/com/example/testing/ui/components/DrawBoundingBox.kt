package com.example.testing.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as ComposeRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size as CoilSize

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BoundingBoxOverlay(
    imageUri: String,
    boundingBoxes: List<ComposeRect>,
    modifier: Modifier = Modifier,
    boxColor: Color = Color.Red,
    strokeWidth: Float = 3f
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .size(CoilSize.ORIGINAL)
            .allowHardware(false)
            .build()
    )
    val painterState = painter.state

    var originalWidth by remember { mutableStateOf(0f) }
    var originalHeight by remember { mutableStateOf(0f) }

    LaunchedEffect(painterState) {
        if (painterState is AsyncImagePainter.State.Success) {
            painterState.result.drawable.let {
                originalWidth = it.intrinsicWidth.toFloat()
                originalHeight = it.intrinsicHeight.toFloat()
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = "Overlay Image",
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageSize = it },
            contentScale = ContentScale.Fit
        )

        if (
            painterState is AsyncImagePainter.State.Success &&
            imageSize != IntSize.Zero &&
            originalWidth > 0 &&
            originalHeight > 0
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val widthRatio = imageSize.width / originalWidth
                val heightRatio = imageSize.height / originalHeight
                val scaleFactor = minOf(widthRatio, heightRatio)

                val offsetX = (imageSize.width - originalWidth * scaleFactor) / 2f
                val offsetY = (imageSize.height - originalHeight * scaleFactor) / 2f

                boundingBoxes.forEach { box ->
                    val scaledLeft = box.left * scaleFactor + offsetX
                    val scaledTop = box.top * scaleFactor + offsetY
                    val scaledWidth = (box.right - box.left) * scaleFactor
                    val scaledHeight = (box.bottom - box.top) * scaleFactor

                    drawRect(
                        color = boxColor,
                        topLeft = Offset(scaledLeft, scaledTop),
                        size = Size(scaledWidth, scaledHeight),
                        style = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
