package cz.zcu.kiv.dinh.ui.camera

import android.content.Context
import android.graphics.Bitmap
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
import androidx.exifinterface.media.ExifInterface
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Environment
import java.io.FileOutputStream

class CameraManager(private val context: Context) {
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView

    private var cameraProvider: ProcessCameraProvider? = null

    fun startCamera(): PreviewView {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        previewView = PreviewView(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
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

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    fun takePhoto(
        executor: ExecutorService,
        onImageCaptured: (Uri?) -> Unit
    ) {
        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Normalize the image orientation before passing it on
                    val normalizedUri = normalizeImage(photoFile)
                    Log.d("CameraManager", "Photo normalized: $normalizedUri")
                    onImageCaptured(normalizedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "Photo capture failed", exception)
                    onImageCaptured(null)
                }
            }
        )
    }

    private fun normalizeImage(imageFile: File): Uri {
        return try {
            val exif = ExifInterface(imageFile.absolutePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotationDegrees == 0) {
                Uri.fromFile(imageFile)
            } else {
                // Decode the file into a bitmap
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                // Save the rotated bitmap to a new file
                val normalizedFile = File(imageFile.parent, "normalized_${imageFile.name}")
                FileOutputStream(normalizedFile).use { out ->
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                // Optionally, update the EXIF of the normalized file so its orientation is set to normal.
                val newExif = ExifInterface(normalizedFile.absolutePath)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
                newExif.saveAttributes()
                Uri.fromFile(normalizedFile)
            }
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to normalize image", e)
            Uri.fromFile(imageFile)
        }
    }
}
