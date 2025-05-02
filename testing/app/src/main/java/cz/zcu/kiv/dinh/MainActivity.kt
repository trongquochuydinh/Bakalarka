package cz.zcu.kiv.dinh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.zcu.kiv.dinh.ui.camera.CameraScreen
import cz.zcu.kiv.dinh.ui.results.ImageLabelingResultScreen
import cz.zcu.kiv.dinh.ui.results.ObjectDetectionResultScreen
import cz.zcu.kiv.dinh.ui.results.TextRecognitionResultScreen
import cz.zcu.kiv.dinh.ui.settings.HelpScreen
import cz.zcu.kiv.dinh.ui.settings.VisualSettingsScreen
import cz.zcu.kiv.dinh.ui.settings.ModelSettingsScreen
import cz.zcu.kiv.dinh.ui.theme.AppTheme
import cz.zcu.kiv.dinh.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        requestCameraPermission()
        themeManager = ThemeManager(this)

        lifecycleScope.launch {
            val savedTheme = themeManager.getTheme.first()
            setContent {
                var isDarkTheme by rememberSaveable { mutableStateOf(savedTheme) }
                var selectedModel by rememberSaveable { mutableStateOf("Image Labeling") }

                AppTheme(darkTheme = isDarkTheme) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "help") {
                        composable("camera") {
                            CameraScreen(
                                navController = navController,
                                cameraExecutor = cameraExecutor,
                                selectedModel = selectedModel,
                                onModelSelected = { selectedModel = it }
                            )
                        }
                        composable("results/{detectedLabels}/{imageUri}/{processingTime}") { backStackEntry ->
                            val labelsString = backStackEntry.arguments?.getString("detectedLabels") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val processingTimeString = backStackEntry.arguments?.getString("processingTime") ?: ""
                            val labels = if (labelsString == "none") emptyList() else labelsString.split("|")
                            val processingTime = processingTimeString.toFloatOrNull() ?: 0f
                            ImageLabelingResultScreen(navController, imageUri, labels, processingTime)
                        }
                        composable("object_detection_results/{detectedResults}/{imageUri}/{processingTime}") { backStackEntry ->
                            val resultsString = backStackEntry.arguments?.getString("detectedResults") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val processingTimeString = backStackEntry.arguments?.getString("processingTime") ?: ""
                            val processingTime = processingTimeString.toFloatOrNull() ?: 0f
                            val results = if (resultsString.isNotEmpty()) resultsString.split("|") else emptyList()
                            ObjectDetectionResultScreen(navController, imageUri, results, processingTime)
                        }
                        composable("text_recognition_results/{detectedResults}/{imageUri}/{processingTime}") { backStackEntry ->
                            val resultsString = backStackEntry.arguments?.getString("detectedResults") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val processingTimeString = backStackEntry.arguments?.getString("processingTime") ?: ""
                            val processingTime = processingTimeString.toFloatOrNull() ?: 0f
                            val results = if (resultsString.isNotEmpty()) resultsString.split("|") else emptyList()
                            TextRecognitionResultScreen(navController, imageUri, results, processingTime)
                        }
                        composable("visual_settings") {
                            VisualSettingsScreen(navController, isDarkTheme) { newTheme ->
                                isDarkTheme = newTheme
                                lifecycleScope.launch { themeManager.saveTheme(newTheme) }
                            }
                        }
                        composable("model_settings") {
                            ModelSettingsScreen(navController)
                        }
                        composable("help") {
                            HelpScreen(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }
}
