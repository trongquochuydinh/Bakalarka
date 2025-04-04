// MLKitManager.kt
package com.example.testing.ml

import android.content.Context
import android.net.Uri
import com.example.testing.ml.processors.ImageLabelingProcessor
import com.example.testing.ml.processors.ObjectDetectionProcessor
import com.example.testing.ml.processors.TextRecognitionProcessor

class MLKitManager {

    private val imageLabelingProcessor = ImageLabelingProcessor()
    private val objectDetectionProcessor = ObjectDetectionProcessor()
    private val textRecognitionProcessor = TextRecognitionProcessor()

    fun processImage(
        context: Context,
        imageUri: Uri,
        processorType: ProcessorType,
        onResult: (List<String>) -> Unit
    ) {
        when (processorType) {
            ProcessorType.IMAGE_LABELING -> imageLabelingProcessor.processImage(context, imageUri, onResult)
            ProcessorType.OBJECT_DETECTION -> objectDetectionProcessor.processImage(context, imageUri, onResult)
            ProcessorType.TEXT_RECOGNITION -> textRecognitionProcessor.processImage(context, imageUri, onResult)
        }
    }

    enum class ProcessorType {
        IMAGE_LABELING,
        OBJECT_DETECTION,
        TEXT_RECOGNITION
    }
}
