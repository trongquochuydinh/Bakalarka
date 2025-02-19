package com.example.testing.ml

import android.content.Context
import android.net.Uri

class MLKitManager {

    // Můžeme zde přidat další ML procesory (např. FaceDetectionProcessor)
    private val imageLabelingProcessor = ImageLabelingProcessor()

    // Vybere správný procesor na základě zvoleného typu
    fun processImage(
        context: Context,
        imageUri: Uri,
        processorType: ProcessorType,
        onResult: (List<String>) -> Unit
    ) {
        when (processorType) {
            ProcessorType.IMAGE_LABELING -> imageLabelingProcessor.processImage(context, imageUri, onResult)
            // Přidáme další případy (např. ProcessorType.FACE_DETECTION)
        }
    }

    enum class ProcessorType {
        IMAGE_LABELING
        // Přidáme další typy (např. FACE_DETECTION, OBJECT_DETECTION atd.)
    }
}
