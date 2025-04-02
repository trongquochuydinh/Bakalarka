package com.example.testing.ui.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.testing.ml.MLKitManager
import com.example.testing.ui.components.TopBarWithMenu
import java.util.concurrent.ExecutorService

@Composable
fun CameraScreen(
    navController: NavController,
    cameraExecutor: ExecutorService,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.stopCamera()
        }
    }

    val mlKitManager = remember { MLKitManager() }

    val models = listOf("Image Labeling", "Text Recognition", "Object Detection")

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        topBar = { TopBarWithMenu(navController, title = "Camera with ML Kit") },
        bottomBar = {
            Column {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(models) { model ->
                        Text(
                            text = model,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { onModelSelected(model) }
                                .background(
                                    if (selectedModel == model) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .padding(8.dp),
                            color = if (selectedModel == model) Color.White else Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                BottomAppBar {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = {
                                cameraManager.takePhoto(cameraExecutor) { uri ->
                                    uri?.let {
                                        val processorType = when (selectedModel) {
                                            "Image Labeling" -> MLKitManager.ProcessorType.IMAGE_LABELING
                                            "Object Detection" -> MLKitManager.ProcessorType.OBJECT_DETECTION
                                            "Text Recognition" -> MLKitManager.ProcessorType.TEXT_RECOGNITION
                                            else -> MLKitManager.ProcessorType.IMAGE_LABELING
                                        }
                                        mlKitManager.processImage(context, it, processorType) { results ->
                                            val encodedResults = Uri.encode(results.joinToString("|"))
                                            val encodedUri = Uri.encode(it.toString())
                                            when (selectedModel) {
                                                "Object Detection" -> navController.navigate("object_detection_results/$encodedResults/$encodedUri")
                                                "Text Recognition" -> navController.navigate("text_recognition_results/$encodedResults/$encodedUri")
                                                else -> navController.navigate("results/$encodedResults/$encodedUri")
                                            }
                                        }
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.size(72.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Capture",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                factory = { cameraManager.startCamera() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
