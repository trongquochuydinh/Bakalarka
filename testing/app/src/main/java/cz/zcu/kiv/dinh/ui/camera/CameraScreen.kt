package cz.zcu.kiv.dinh.ui.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import cz.zcu.kiv.dinh.ml.MLKitManager
import cz.zcu.kiv.dinh.ui.components.TopBarWithMenu
import java.util.concurrent.ExecutorService
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * Hlavní Composable komponenta pro práci s kamerou a ML Kit modely.
 * Umožňuje pořízení fotografie nebo výběr z galerie a následné spuštění detekce pomocí vybraného modelu.
 *
 * @param navController Navigace mezi obrazovkami.
 * @param cameraExecutor Executor pro zajištění paralelního zpracování.
 * @param selectedModel Aktuálně zvolený model pro detekci.
 * @param onModelSelected Callback pro změnu modelu.
 * @param hasCameraPermission Informace o oprávnění ke kameře.
 */
@Composable
fun CameraScreen(
    navController: NavController,
    cameraExecutor: ExecutorService,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    hasCameraPermission: Boolean
) {
    val context = LocalContext.current
    val cameraManager = remember { CameraManager(context) }
    var isProcessing by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                cameraManager.stopCamera()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            cameraManager.stopCamera()
        }
    }


    val mlKitManager = remember { MLKitManager() }
    val models = listOf("Image Labeling", "Text Recognition", "Object Detection")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isProcessing = true

            val normalizedUri = cameraManager.normalizeImage(context, it)

            val processorType = when (selectedModel) {
                "Image Labeling" -> MLKitManager.ProcessorType.IMAGE_LABELING
                "Object Detection" -> MLKitManager.ProcessorType.OBJECT_DETECTION
                "Text Recognition" -> MLKitManager.ProcessorType.TEXT_RECOGNITION
                else -> MLKitManager.ProcessorType.IMAGE_LABELING
            }
            mlKitManager.processImage(context, normalizedUri, processorType) { results, processingTime ->
                val resultText = if (results.isEmpty()) "none" else results.joinToString("|")
                val encodedResults = Uri.encode(resultText)
                val encodedUri = Uri.encode(normalizedUri.toString())
                val encodedProcessingTime = Uri.encode(processingTime.toString())
                when (selectedModel) {
                    "Object Detection" -> navController.navigate("object_detection_results/$encodedResults/$encodedUri/$encodedProcessingTime")
                    "Text Recognition" -> navController.navigate("text_recognition_results/$encodedResults/$encodedUri/$encodedProcessingTime")
                    else -> navController.navigate("results/$encodedResults/$encodedUri/$encodedProcessingTime")
                }
                isProcessing = false
            }
        }
    }

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            enabled = !isProcessing,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Button(
                            onClick = {
                                isProcessing = true
                                cameraManager.takePhoto(cameraExecutor) { uri ->
                                    uri?.let {
                                        val processorType = when (selectedModel) {
                                            "Image Labeling" -> MLKitManager.ProcessorType.IMAGE_LABELING
                                            "Object Detection" -> MLKitManager.ProcessorType.OBJECT_DETECTION
                                            "Text Recognition" -> MLKitManager.ProcessorType.TEXT_RECOGNITION
                                            else -> MLKitManager.ProcessorType.IMAGE_LABELING
                                        }
                                        mlKitManager.processImage(context, it, processorType) { results, processingTime ->
                                            val resultText = if (results.isEmpty()) "none" else results.joinToString("|")
                                            val encodedResults = Uri.encode(resultText)
                                            val encodedUri = Uri.encode(it.toString())
                                            val encodedProcessingTime = Uri.encode(processingTime.toString())

                                            when (selectedModel) {
                                                "Object Detection" -> navController.navigate("object_detection_results/$encodedResults/$encodedUri/$encodedProcessingTime")
                                                "Text Recognition" -> navController.navigate("text_recognition_results/$encodedResults/$encodedUri/$encodedProcessingTime")
                                                else -> navController.navigate("results/$encodedResults/$encodedUri/$encodedProcessingTime")
                                            }
                                            isProcessing = false
                                        }
                                    } ?: run {
                                        isProcessing = false
                                    }
                                }
                            },
                            enabled = !isProcessing && hasCameraPermission,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.size(72.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            } else {
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF625A5A)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { cameraManager.startCamera() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "No Camera Permission",
                        tint = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "K použití kamery nejsou udělena oprávnění.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}


