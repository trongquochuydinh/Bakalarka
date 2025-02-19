package com.example.testing.ml

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageLabelingProcessor : BaseMLProcessor() {

    override fun processImage(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>) -> Unit
    ) {
        val image = InputImage.fromFilePath(context, imageUri)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                val labelTexts = labels.map { label -> "${label.text} (${(label.confidence * 100).toInt()}%)" }
                onResult(labelTexts)
                Toast.makeText(context, "Detekce dokončena", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ImageLabeling", "Image labeling failed", e)
                Toast.makeText(context, "Chyba při detekci", Toast.LENGTH_SHORT).show()
            }
    }
}
