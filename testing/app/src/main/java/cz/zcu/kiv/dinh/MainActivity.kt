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

/**
 * Hlavní aktivita aplikace, která zajišťuje spuštění UI a navigaci mezi obrazovkami.
 * Používá Jetpack Compose pro vykreslení obsahu.
 */
class MainActivity : ComponentActivity() {

    /** Executor pro zpracování úloh spojených s kamerou na samostatném vlákně. */
    private lateinit var cameraExecutor: ExecutorService

    /** Správce pro uchovávání preferencí uživatelského vzhledu (světlý/tmavý režim). */
    private lateinit var themeManager: ThemeManager

    /**
     * Inicializace aktivity – nastaví motiv, kamerový executor a spustí hlavní obsah pomocí Compose.
     * Provádí také načtení preferovaného tématu a kontrolu oprávnění pro použití kamery.
     */
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
                        val hasCameraPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        // Hlavní obrazovka s náhledem kamery
                        composable("camera") {
                            CameraScreen(
                                navController = navController,
                                cameraExecutor = cameraExecutor,
                                selectedModel = selectedModel,
                                onModelSelected = { selectedModel = it },
                                hasCameraPermission = hasCameraPermission
                            )
                        }

                        // Výsledková obrazovka pro image labeling
                        composable("results/{detectedLabels}/{imageUri}/{processingTime}") { backStackEntry ->
                            val labelsString = backStackEntry.arguments?.getString("detectedLabels") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val processingTimeString = backStackEntry.arguments?.getString("processingTime") ?: ""
                            val labels = if (labelsString == "none") emptyList() else labelsString.split("|")
                            val processingTime = processingTimeString.toFloatOrNull() ?: 0f
                            ImageLabelingResultScreen(navController, imageUri, labels, processingTime)
                        }

                        // Výsledková obrazovka pro detekci objektů
                        composable("object_detection_results/{detectedResults}/{imageUri}/{processingTime}") { backStackEntry ->
                            val resultsString = backStackEntry.arguments?.getString("detectedResults") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val processingTimeString = backStackEntry.arguments?.getString("processingTime") ?: ""
                            val processingTime = processingTimeString.toFloatOrNull() ?: 0f
                            val results = if (resultsString.isNotEmpty()) resultsString.split("|") else emptyList()
                            ObjectDetectionResultScreen(navController, imageUri, results, processingTime)
                        }

                        // Výsledková obrazovka pro rozpoznávání textu
                        composable("text_recognition_results/{detectedResults}/{imageUri}/{processingTime}") { backStackEntry ->
                            val resultsString = backStackEntry.arguments?.getString("detectedResults") ?: ""
                            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                            val processingTimeString = backStackEntry.arguments?.getString("processingTime") ?: ""
                            val processingTime = processingTimeString.toFloatOrNull() ?: 0f
                            val results = if (resultsString.isNotEmpty()) resultsString.split("|") else emptyList()
                            TextRecognitionResultScreen(navController, imageUri, results, processingTime)
                        }

                        // Nastavení vzhledu aplikace (tmavý/světlý režim)
                        composable("visual_settings") {
                            VisualSettingsScreen(navController, isDarkTheme) { newTheme ->
                                isDarkTheme = newTheme
                                lifecycleScope.launch { themeManager.saveTheme(newTheme) }
                            }
                        }

                        // Nastavení výběru modelu
                        composable("model_settings") {
                            ModelSettingsScreen(navController)
                        }

                        // Úvodní nápověda
                        composable("help") {
                            HelpScreen(navController)
                        }
                    }
                }
            }
        }
    }

    /**
     * Ukončení aktivity – vypne cameraExecutor.
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    /**
     * Vyžádá oprávnění pro použití kamery, pokud ještě není uděleno.
     */
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }
}
