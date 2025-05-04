// MLKitManager.kt
package cz.zcu.kiv.dinh.ml

import android.content.Context
import android.net.Uri
import cz.zcu.kiv.dinh.ml.processors.ImageLabelingProcessor
import cz.zcu.kiv.dinh.ml.processors.ObjectDetectionProcessor
import cz.zcu.kiv.dinh.ml.processors.TextRecognitionProcessor

/**
 * Hlavní správce ML procesorů, který deleguje zpracování obrázku na vybraný model.
 * Zajišťuje jednoduché rozhraní pro volání modelů (Image Labeling, Text Recognition, Object Detection).
 */
class MLKitManager {

    // Instancování jednotlivých procesorů
    private val imageLabelingProcessor = ImageLabelingProcessor()
    private val objectDetectionProcessor = ObjectDetectionProcessor()
    private val textRecognitionProcessor = TextRecognitionProcessor()

    /**
     * Zpracuje obrázek pomocí vybraného ML procesoru.
     *
     * @param context Kontext aplikace
     * @param imageUri URI obrázku, který má být analyzován
     * @param processorType Typ procesoru (labeling / detection / recognition)
     * @param onResult Callback, který vrací seznam výsledků a dobu zpracování
     */
    fun processImage(
        context: Context,
        imageUri: Uri,
        processorType: ProcessorType,
        onResult: (List<String>, Long) -> Unit
    ) {
        when (processorType) {
            ProcessorType.IMAGE_LABELING -> imageLabelingProcessor.processImage(context, imageUri, onResult)
            ProcessorType.OBJECT_DETECTION -> objectDetectionProcessor.processImage(context, imageUri, onResult)
            ProcessorType.TEXT_RECOGNITION -> textRecognitionProcessor.processImage(context, imageUri, onResult)
        }
    }

    /**
     * Enum reprezentující dostupné procesory pro zpracování obrázků.
     */
    enum class ProcessorType {
        IMAGE_LABELING,
        OBJECT_DETECTION,
        TEXT_RECOGNITION
    }
}
