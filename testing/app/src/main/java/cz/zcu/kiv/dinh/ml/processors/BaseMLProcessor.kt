package cz.zcu.kiv.dinh.ml.processors

import android.content.Context
import android.net.Uri

abstract class BaseMLProcessor {

    abstract fun processImage(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>, Long) -> Unit
    )

    abstract fun processWithCloudVisionAPI(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>, Long) -> Unit
    )
}