package cz.zcu.kiv.dinh.ui.components

import android.annotation.SuppressLint
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

/**
 * Zobrazí obrázek s překrytím ohraničujících rámečků (bounding boxes).
 *
 * @param imageUri URI obrázku k zobrazení.
 * @param boundingBoxes Seznam rámečků, které budou překresleny přes obrázek.
 * @param selectedIndex Volitelný index zvýrazněného rámečku (např. po kliknutí).
 * @param modifier Volitelný Compose modifier.
 * @param boxColor Barva běžného rámečku.
 * @param highlightColor Barva zvýrazněného rámečku.
 * @param strokeWidth Tloušťka rámečku v dp.
 */
@SuppressLint("UnusedBoxWithConstraintsScope", "ModifierParameter")
@Composable
fun BoundingBoxOverlay(
    imageUri: String,
    boundingBoxes: List<ComposeRect>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    boxColor: Color = Color.Blue,
    highlightColor: Color = Color.Red,
    strokeWidth: Float = 3f
) {
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current

    // Vykreslení obrázku pomocí Coil knihovny
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageUri)
            .size(CoilSize.ORIGINAL)
            .allowHardware(false)
            .build()
    )
    val painterState = painter.state

    var originalWidth by remember { mutableFloatStateOf(0f) }
    var originalHeight by remember { mutableFloatStateOf(0f) }

    // Získání původní velikosti obrázku při načtení
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

                // Vykreslení každého rámečku včetně zvýraznění vybraného
                boundingBoxes.forEachIndexed { index, box ->
                    val scaledLeft = box.left * scaleFactor + offsetX
                    val scaledTop = box.top * scaleFactor + offsetY
                    val scaledWidth = (box.right - box.left) * scaleFactor
                    val scaledHeight = (box.bottom - box.top) * scaleFactor

                    drawRect(
                        color = if (index == selectedIndex) highlightColor else boxColor,
                        topLeft = Offset(scaledLeft, scaledTop),
                        size = Size(scaledWidth, scaledHeight),
                        style = Stroke(width = strokeWidth.dp.toPx())
                    )
                }
            }
        }
    }
}