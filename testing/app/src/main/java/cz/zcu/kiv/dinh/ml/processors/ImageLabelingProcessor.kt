package cz.zcu.kiv.dinh.ml.processors

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import cz.zcu.kiv.dinh.ml.configs.ImageLabelingConfig
import cz.zcu.kiv.dinh.ml.utils.CloudVisionUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Procesor pro označování obrázků (Image Labeling).
 * Rozhoduje, zda použít cloudový nebo on-device model na základě konfigurace.
 */
class ImageLabelingProcessor : BaseMLProcessor() {

    /**
     * Zpracuje zadaný obrázek a vrátí nalezené štítky.
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku
     * @param onResult Callback, který vrací seznam štítků a dobu zpracování v ms
     */
    override fun processImage(context: Context, imageUri: Uri, onResult: (List<String>, Long) -> Unit) {
        if (ImageLabelingConfig.useCloudModel) {
            processWithCloudVisionAPI(context, imageUri, onResult)
        } else {
            val image = InputImage.fromFilePath(context, imageUri)
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(ImageLabelingConfig.minConfidencePercentage / 100.0f)
                .build()
            val labeler = ImageLabeling.getClient(options)
            val startTime = System.currentTimeMillis()
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val endTime = System.currentTimeMillis()
                    val labelTexts = labels.map {
                        "${it.text} (${(it.confidence * 100).toInt()}%)"
                    }
                    onResult(labelTexts, endTime - startTime)
                    Toast.makeText(context, "Detekce dokončena", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Log.e("ImageLabeling", "Image labeling failed", it)
                    Toast.makeText(context, "Chyba při detekci", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Zpracuje obrázek pomocí cloudové služby Vision API s typem "LABEL_DETECTION".
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku
     * @param onResult Callback, který vrací výsledky a dobu zpracování
     */
    override fun processWithCloudVisionAPI(context: Context, imageUri: Uri, onResult: (List<String>, Long) -> Unit) {
        processCloudRequest(
            context, imageUri,
            featureType = "LABEL_DETECTION",
            maxResults = 10,
            onResult = onResult
        ) { json, _, _ ->
            CloudVisionUtils.parseLabelResponse(json, ImageLabelingConfig.minConfidencePercentage)
        }
    }
}
