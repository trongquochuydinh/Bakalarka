package com.example.testing.ml.processors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.testing.ml.configs.ObjectDetectionConfig
import com.example.testing.ml.utils.CloudVisionUtils
import com.example.testing.ml.processors.BaseMLProcessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class ObjectDetectionProcessor : BaseMLProcessor() {
    override fun processImage(context: Context, imageUri: Uri, onResult: (List<String>) -> Unit) {
        if (ObjectDetectionConfig.useCloudModel) {
            processWithCloudVisionAPI(context, imageUri, onResult)
        } else {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val options = ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification() // Enables labels (if available)
                    .build()
                val objectDetector = ObjectDetection.getClient(options)
                objectDetector.process(image)
                    .addOnSuccessListener { detectedObjects ->
                        val detectionResults = mutableListOf<String>()
                        for (detectedObject in detectedObjects) {
                            val boundingBox: Rect = detectedObject.boundingBox
                            val label = if (detectedObject.labels.isNotEmpty()) {
                                detectedObject.labels[0].text
                            } else {
                                "Unknown"
                            }
                            val confidence = if (detectedObject.labels.isNotEmpty()) {
                                (detectedObject.labels[0].confidence * 100).toInt()
                            } else {
                                0
                            }
                            // Format: "Label (Confidence%) - Box: [left, top, right, bottom]"
                            val resultString = "$label ($confidence%) - Box: [${boundingBox.left}, ${boundingBox.top}, ${boundingBox.right}, ${boundingBox.bottom}]"
                            detectionResults.add(resultString)
                        }
                        Toast.makeText(context, "Object detection completed", Toast.LENGTH_SHORT).show()
                        onResult(detectionResults)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ObjectDetection", "Object detection failed", e)
                        Toast.makeText(context, "Error during object detection", Toast.LENGTH_SHORT).show()
                        onResult(emptyList())
                    }
            } catch (e: Exception) {
                Log.e("ObjectDetection", "Failed to process image", e)
                Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                onResult(emptyList())
            }
        }
    }

    private fun processWithCloudVisionAPI(context: Context, imageUri: Uri, onResult: (List<String>) -> Unit) {
        try {
            // Convert image to Base64 string
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val width = bitmap.width
            val height = bitmap.height

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            // Build request payload using CloudVisionUtils for OBJECT_LOCALIZATION
            val jsonBody = CloudVisionUtils.createRequestPayload(base64Image, "OBJECT_LOCALIZATION")
            val request: Request = CloudVisionUtils.buildRequest(jsonBody)

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("CloudVision", "Request failed", e)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Object detection (cloud) failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    val responseBody = response.body?.string() ?: ""
                    try {
                        val json = JSONObject(responseBody)
                        val results = CloudVisionUtils.parseObjectLocalizationResponse(
                            json,
                            ObjectDetectionConfig.minConfidencePercentage,
                            width,
                            height // ✅ Pass dimensions here
                        )
                        Handler(Looper.getMainLooper()).post {
                            onResult(results)
                            Toast.makeText(context, "Object detection (cloud) completed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("CloudVision", "Parsing failed", e)
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(context, "Error processing response", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("CloudVision", "Error converting image", e)
            Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
            onResult(emptyList())
        }
    }
}