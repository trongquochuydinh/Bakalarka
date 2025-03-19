// ObjectDetectionProcessor.kt
package com.example.testing.ml

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class ObjectDetectionProcessor : BaseMLProcessor() {
    override fun processImage(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>) -> Unit
    ) {
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
