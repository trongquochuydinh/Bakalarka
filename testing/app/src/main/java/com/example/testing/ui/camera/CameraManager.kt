package com.example.testing.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class CameraManager(private val context: Context) {
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView

    fun startCamera(): PreviewView {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        previewView = PreviewView(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraManager", "Camera initialization failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        return previewView
    }

    fun takePhoto(
        executor: ExecutorService,
        onImageCaptured: (Uri?) -> Unit
    ) {
        val photoFile = File(
            context.externalMediaDirs.first(),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d("CameraManager", "Photo saved: $savedUri")
                    onImageCaptured(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "Photo capture failed", exception)
                    onImageCaptured(null)
                }
            }
        )
    }
}
