// TextRecognitionConfig.kt
package com.example.testing.ml.configs

object TextRecognitionConfig {
    var useCloudModel: Boolean = false
    // Allowed values: "block", "line", "word", "symbol"
    var segmentationMode: String = "block"
    // For symbol segmentation: if non-null, only that symbol will be highlighted.
    var highlightSymbol: Char? = null
    // For word segmentation: if non-null, only elements containing that word (case-insensitive) are highlighted.
    var highlightWord: String? = null
}
