package com.example.testing.ml.processors

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.testing.ml.configs.TextRecognitionConfig
import com.example.testing.ml.utils.CloudVisionUtils
import com.example.testing.ml.processors.BaseMLProcessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class TextRecognitionProcessor : BaseMLProcessor() {
    override fun processImage(context: Context, imageUri: Uri, onResult: (List<String>) -> Unit) {
        Log.d("TextRecognition", "Processing image: $imageUri")
        if (TextRecognitionConfig.useCloudModel) {
            processWithCloudVisionAPI(context, imageUri, onResult)
        } else {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        Log.d("TextRecognition", "Text recognition succeeded")
                        val segments = mutableListOf<String>()
                        val mode = TextRecognitionConfig.segmentationMode.lowercase()
                        when (mode) {
                            "block" -> {
                                result.textBlocks.forEach { block ->
                                    val rect = block.boundingBox
                                    segments.add("Block: ${block.text} ([${rect?.left}, ${rect?.top}, ${rect?.right}, ${rect?.bottom}])")
                                }
                            }
                            "line" -> {
                                result.textBlocks.forEach { block ->
                                    block.lines.forEach { line ->
                                        val rect = line.boundingBox
                                        segments.add("Line: ${line.text} ([${rect?.left}, ${rect?.top}, ${rect?.right}, ${rect?.bottom}])")
                                    }
                                }
                            }
                            "word" -> {
                                result.textBlocks.forEach { block ->
                                    block.lines.forEach { line ->
                                        line.elements.forEach { element ->
                                            if (TextRecognitionConfig.highlightWord == null ||
                                                element.text.contains(TextRecognitionConfig.highlightWord!!, ignoreCase = true)
                                            ) {
                                                val rect = element.boundingBox
                                                segments.add("Word: ${element.text} ([${rect?.left}, ${rect?.top}, ${rect?.right}, ${rect?.bottom}])")
                                            }
                                        }
                                    }
                                }
                            }
                            "symbol" -> {
                                result.textBlocks.forEach { block ->
                                    block.lines.forEach { line ->
                                        line.elements.forEach { element ->
                                            val rect = element.boundingBox
                                            if (rect != null && element.text.isNotEmpty()) {
                                                val charWidth = rect.width() / element.text.length
                                                element.text.forEachIndexed { index, c ->
                                                    if (TextRecognitionConfig.highlightSymbol == null || c == TextRecognitionConfig.highlightSymbol) {
                                                        val left = rect.left + index * charWidth
                                                        val top = rect.top
                                                        val right = left + charWidth
                                                        val bottom = rect.bottom
                                                        segments.add("Symbol: $c ([${left}, ${top}, ${right}, ${bottom}])")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                result.textBlocks.forEach { block ->
                                    val rect = block.boundingBox
                                    segments.add("Block: ${block.text} ([${rect?.left}, ${rect?.top}, ${rect?.right}, ${rect?.bottom}])")
                                }
                            }
                        }
                        Log.d("TextRecognition", "Generated ${segments.size} segments")
                        onResult(segments)
                    }
                    .addOnFailureListener { e ->
                        Log.e("TextRecognition", "Text recognition failed", e)
                        Toast.makeText(context, "Text recognition failed", Toast.LENGTH_SHORT).show()
                        onResult(emptyList())
                    }
            } catch (e: Exception) {
                Log.e("TextRecognition", "Error processing image", e)
                Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                onResult(emptyList())
            }
        }
    }

    private fun processWithCloudVisionAPI(context: Context, imageUri: Uri, onResult: (List<String>) -> Unit) {
        try {
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val width = bitmap.width
            val height = bitmap.height

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val jsonBody = CloudVisionUtils.createRequestPayload(base64Image, "TEXT_DETECTION")
            val request: Request = CloudVisionUtils.buildRequest(jsonBody)

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    Log.e("CloudVision", "Request failed", e)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Text detection (cloud) failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    val responseBody = response.body?.string() ?: ""
                    try {
                        val json = JSONObject(responseBody)
                        val results = CloudVisionUtils.parseTextRecognitionResponse(json)
                        Handler(Looper.getMainLooper()).post {
                            onResult(results)
                            Toast.makeText(context, "Text detection (cloud) completed", Toast.LENGTH_SHORT).show()
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