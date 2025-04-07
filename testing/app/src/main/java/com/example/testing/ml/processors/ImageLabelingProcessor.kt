// ImageLabelingProcessor.kt
package com.example.testing.ml.processors

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.testing.ml.configs.ImageLabelingConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

// For image processing
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream

// For HTTP request
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

// For JSON parsing
import org.json.JSONArray
import org.json.JSONObject

// For posting UI updates back to main thread
import android.os.Handler
import android.os.Looper
import java.io.IOException

class ImageLabelingProcessor : BaseMLProcessor() {

    override fun processImage(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>) -> Unit
    ) {
        if (ImageLabelingConfig.useCloudModel) {
            processWithCloudVisionAPI(context, imageUri, onResult)
        } else {
            val image = InputImage.fromFilePath(context, imageUri)
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(ImageLabelingConfig.minConfidencePercentage / 100.0f)
                .build()
            val labeler = ImageLabeling.getClient(options)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val labelTexts = labels.map { label ->
                        "${label.text} (${(label.confidence * 100).toInt()}%)"
                    }
                    onResult(labelTexts)
                    Toast.makeText(context, "Detekce dokončena", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("ImageLabeling", "Image labeling failed", e)
                    Toast.makeText(context, "Chyba při detekci", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun processWithCloudVisionAPI(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>) -> Unit
    ) {
        val apiKey = "AIzaSyDTpLFdZIYxMsCxGdTtES2oze3801PFSgQ" // Replace with your real API key

        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val jsonBody = """
        {
          "requests": [
            {
              "image": {
                "content": "$base64Image"
              },
              "features": [
                {
                  "type": "LABEL_DETECTION",
                  "maxResults": 10
                }
              ]
            }
          ]
        }
    """.trimIndent()

        val client = OkHttpClient()
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://vision.googleapis.com/v1/images:annotate?key=$apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CloudVision", "Request failed", e)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Chyba při detekci (cloud)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    val json = JSONObject(responseBody)
                    val labelAnnotations = json
                        .getJSONArray("responses")
                        .getJSONObject(0)
                        .optJSONArray("labelAnnotations") ?: JSONArray()
                    Log.d("CloudVisionRaw", responseBody)

                    val results = mutableListOf<String>()
                    for (i in 0 until labelAnnotations.length()) {
                        val label = labelAnnotations.getJSONObject(i)
                        val text = label.getString("description")
                        val confidence = (label.getDouble("score") * 100).toInt()
                        if (confidence >= ImageLabelingConfig.minConfidencePercentage) {
                            results.add("$text (${confidence}%)")
                        }
                    }

                    Handler(Looper.getMainLooper()).post {
                        onResult(results)
                        Toast.makeText(context, "Detekce (cloud) dokončena", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("CloudVision", "Parsing failed", e)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Chyba při zpracování odpovědi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}


