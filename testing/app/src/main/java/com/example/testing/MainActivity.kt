package com.example.testing

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
import com.example.testing.ui.camera.CameraScreen
import com.example.testing.ui.results.ImageLabelingResultScreen
import com.example.testing.ui.results.ObjectDetectionResultScreen
import com.example.testing.ui.results.TextRecognitionResultScreen
import com.example.testing.ui.settings.VisualSettingsScreen
import com.example.testing.ui.settings.ModelSettingsScreen
import com.example.testing.ui.theme.TestingTheme
import com.example.testing.ui.theme.ThemeManager
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

                TestingTheme(darkTheme = isDarkTheme) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "camera") {
                        composable("camera") {
                            CameraScreen(
                                navController = navController,
                                cameraExecutor = cameraExecutor,
                                selectedModel = selectedModel,
                                onModelSelected = { selectedModel = it }
                            )
                        }
                        composable("results/{detectedLabels}/{imageUri}") { backStackEntry ->
                            val labelsString = backStackEntry.arguments?.getString("detectedLabels") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val labels = if (labelsString.isNotEmpty()) labelsString.split("|") else emptyList()
                            ImageLabelingResultScreen(navController, imageUri, labels)
                        }
                        composable("object_detection_results/{detectedResults}/{imageUri}") { backStackEntry ->
                            val resultsString = backStackEntry.arguments?.getString("detectedResults") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val results = if (resultsString.isNotEmpty()) resultsString.split("|") else emptyList()
                            ObjectDetectionResultScreen(navController, imageUri, results)
                        }
                        composable("text_recognition_results/{detectedResults}/{imageUri}") { backStackEntry ->
                            val resultsString = backStackEntry.arguments?.getString("detectedResults") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val results = if (resultsString.isNotEmpty()) resultsString.split("|") else emptyList()
                            TextRecognitionResultScreen(navController, imageUri, results)
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
