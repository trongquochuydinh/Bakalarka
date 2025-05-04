package cz.zcu.kiv.dinh.ml.processors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cz.zcu.kiv.dinh.ml.configs.ObjectDetectionConfig
import cz.zcu.kiv.dinh.ml.utils.CloudVisionUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Procesor pro detekci objektů (Object Detection).
 * Vrací seznam objektů s názvem, důvěrou a souřadnicemi.
 */
class ObjectDetectionProcessor : BaseMLProcessor() {

    /**
     * Zpracuje obrázek a vrátí detekované objekty.
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku
     * @param onResult Callback s výsledky a dobou zpracování v ms
     */
    override fun processImage(context: Context, imageUri: Uri, onResult: (List<String>, Long) -> Unit) {
        if (ObjectDetectionConfig.useCloudModel) {
            processWithCloudVisionAPI(context, imageUri, onResult)
        } else {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val options = ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .build()
                val detector = ObjectDetection.getClient(options)
                val startTime = System.currentTimeMillis()

                detector.process(image)
                    .addOnSuccessListener { objects ->
                        val endTime = System.currentTimeMillis()
                        val results = objects.mapNotNull { obj ->
                            val box = obj.boundingBox
                            val label = obj.labels.firstOrNull()?.text ?: "Unknown"
                            val confidence = (obj.labels.firstOrNull()?.confidence ?: 0f) * 100
                            if (confidence >= ObjectDetectionConfig.minConfidencePercentage) {
                                "$label (${confidence.toInt()}%) - Box: [${box.left}, ${box.top}, ${box.right}, ${box.bottom}]"
                            } else null
                        }

                        Toast.makeText(context, "Object detection completed", Toast.LENGTH_SHORT).show()
                        onResult(results, endTime - startTime)
                    }
                    .addOnFailureListener {
                        Log.e("ObjectDetection", "Object detection failed", it)
                        Toast.makeText(context, "Error during object detection", Toast.LENGTH_SHORT).show()
                        onResult(emptyList(), 0)
                    }
            } catch (e: Exception) {
                Log.e("ObjectDetection", "Failed to process image", e)
                Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                onResult(emptyList(), 0)
            }
        }
    }

    /**
     * Zpracuje obrázek přes Google Cloud Vision API s typem "OBJECT_LOCALIZATION".
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku
     * @param onResult Callback s výsledky a dobou zpracování
     */
    override fun processWithCloudVisionAPI(context: Context, imageUri: Uri, onResult: (List<String>, Long) -> Unit) {
        processCloudRequest(
            context, imageUri,
            featureType = "OBJECT_LOCALIZATION",
            maxResults = 10,
            onResult = onResult
        ) { json, width, height ->
            CloudVisionUtils.parseObjectLocalizationResponse(
                json,
                ObjectDetectionConfig.minConfidencePercentage,
                width,
                height
            )
        }
    }
}