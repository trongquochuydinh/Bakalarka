package com.example.testing.ml.processors

import android.content.Context
import android.net.Uri

abstract class BaseMLProcessor {

    abstract fun processImage(
        context: Context,
        imageUri: Uri,
        onResult: (List<String>) -> Unit
    )
}