package com.example.testing.ml.processors

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.testing.ml.configs.ImageLabelingConfig
import com.example.testing.ml.utils.CloudVisionUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.ByteArrayOutputStream
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
        // Convert image to Base64 string
        val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        // Build request payload using CloudVisionUtils for LABEL_DETECTION
        val jsonBody = CloudVisionUtils.createRequestPayload(base64Image, "LABEL_DETECTION")
        val request: Request = CloudVisionUtils.buildRequest(jsonBody)

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("CloudVision", "Request failed", e)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Chyba při detekci (cloud)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val responseBody = response.body?.string() ?: ""
                try {
                    val json = JSONObject(responseBody)
                    val results = CloudVisionUtils.parseLabelResponse(
                        json, ImageLabelingConfig.minConfidencePercentage
                    )
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