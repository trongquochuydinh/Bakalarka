package cz.zcu.kiv.dinh.ml.processors

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cz.zcu.kiv.dinh.ml.configs.TextRecognitionConfig
import cz.zcu.kiv.dinh.ml.utils.CloudVisionUtils
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

/**
 * Procesor pro rozpoznávání textu (Text Recognition).
 * Umožňuje výběr segmentačního režimu (bloky, řádky, slova, symboly) a zvýraznění slov/symbolů.
 */
class TextRecognitionProcessor : BaseMLProcessor() {

    /**
     * Zpracuje zadaný obrázek pomocí textového rozpoznávání.
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku
     * @param onResult Callback, který vrací seznam výsledků (s popisem a bounding boxem) a dobu zpracování v ms
     */
    override fun processImage(context: Context, imageUri: Uri, onResult: (List<String>, Long) -> Unit) {
        Log.d("TextRecognition", "Processing image: $imageUri")
        if (TextRecognitionConfig.useCloudModel) {
            processWithCloudVisionAPI(context, imageUri, onResult)
        } else {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val startTime = System.currentTimeMillis()

                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        val endTime = System.currentTimeMillis()
                        val segments = mutableListOf<String>()
                        val mode = TextRecognitionConfig.segmentationMode.lowercase()

                        // Segmentace výsledků podle zvoleného režimu
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

                        onResult(segments, endTime - startTime)
                    }
                    .addOnFailureListener {
                        Log.e("TextRecognition", "Text recognition failed", it)
                        Toast.makeText(context, "Text recognition failed", Toast.LENGTH_SHORT).show()
                        onResult(emptyList(), 0)
                    }
            } catch (e: Exception) {
                Log.e("TextRecognition", "Error processing image", e)
                Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
                onResult(emptyList(), 0)
            }
        }
    }

    /**
     * Zpracuje obrázek přes Google Cloud Vision API s typem "TEXT_DETECTION".
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku
     * @param onResult Callback s výsledky a dobou zpracování
     */
    override fun processWithCloudVisionAPI(context: Context, imageUri: Uri, onResult: (List<String>, Long) -> Unit) {
        processCloudRequest(
            context, imageUri,
            featureType = "TEXT_DETECTION",
            maxResults = 10,
            onResult = onResult
        ) { json, _, _ ->
            CloudVisionUtils.parseTextRecognitionResponse(json)
        }
    }
}
