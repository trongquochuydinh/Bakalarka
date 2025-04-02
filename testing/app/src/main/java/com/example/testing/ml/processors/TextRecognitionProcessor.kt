// TextRecognitionProcessor.kt
package com.example.testing.ml.processors

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.testing.ml.configs.TextRecognitionConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionProcessor : BaseMLProcessor() {
    override fun processImage(context: Context, imageUri: Uri, onResult: (List<String>) -> Unit) {
        Log.d("TextRecognition", "Processing image: $imageUri")
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
                            // In ML Kit, elements usually represent words.
                            result.textBlocks.forEach { block ->
                                block.lines.forEach { line ->
                                    line.elements.forEach { element ->
                                        if (TextRecognitionConfig.highlightWord == null ||
                                            element.text.contains(TextRecognitionConfig.highlightWord!!, ignoreCase = true)) {
                                            val rect = element.boundingBox
                                            segments.add("Word: ${element.text} ([${rect?.left}, ${rect?.top}, ${rect?.right}, ${rect?.bottom}])")
                                        }
                                    }
                                }
                            }
                        }
                        "symbol" -> {
                            // ML Kit’s text recognition API does not output symbols separately,
                            // so we approximate by splitting each element’s text.
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
                            // Default to block segmentation.
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
