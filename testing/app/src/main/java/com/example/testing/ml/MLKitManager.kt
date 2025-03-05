package com.example.testing.ml

import android.content.Context
import android.net.Uri

class MLKitManager {

    // Můžeme zde přidat další ML procesory (např. FaceDetectionProcessor)
    private val imageLabelingProcessor = ImageLabelingProcessor()
    private val objectDetectionProcessor = ObjectDetectionProcessor()
    private val subjectSegmentationProcessor = SubjectSegmentationProcessor()


    // Vybere správný procesor na základě zvoleného typu
    fun processImage(
        context: Context,
        imageUri: Uri,
        processorType: ProcessorType,
        onResult: (List<String>) -> Unit
    ) {
        when (processorType) {
            ProcessorType.IMAGE_LABELING -> imageLabelingProcessor.processImage(context, imageUri, onResult)
            ProcessorType.OBJECT_DETECTION -> objectDetectionProcessor.processImage(context, imageUri, onResult)
            ProcessorType.SUBJECT_SEGMENTATION -> subjectSegmentationProcessor.processImage(context, imageUri, onResult)
        }
    }

    enum class ProcessorType {
        IMAGE_LABELING,
        OBJECT_DETECTION,
        SUBJECT_SEGMENTATION
    }
}
