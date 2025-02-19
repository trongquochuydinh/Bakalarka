package com.example.testing.ui

import android.net.Uri
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.testing.camera.CameraManager
import com.example.testing.ml.MLKitManager
import java.util.concurrent.ExecutorService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(cameraExecutor: ExecutorService) {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }
    val mlKitManager = remember { MLKitManager() }
    var detectedLabels by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.primary),
        topBar = {
            TopAppBar(
                title = { Text("Camera with ML Kit") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(
                        onClick = {
                            cameraManager.takePhoto(cameraExecutor) { uri ->
                                uri?.let {
                                    mlKitManager.processImage(
                                        context,
                                        it,
                                        MLKitManager.ProcessorType.IMAGE_LABELING
                                    ) { labels ->
                                        detectedLabels = labels
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                factory = { ctx ->
                    cameraManager.startCamera()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Detekované objekty:", style = MaterialTheme.typography.titleMedium, color = Color.White)
            detectedLabels.forEach { label ->
                Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            }
        }
    }
}
