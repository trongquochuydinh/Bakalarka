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
            val rotationDegrees = getRotationDegrees(orientation)

            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            saveNormalizedBitmap(bitmap, rotationDegrees, imageFile.name)
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to normalize image", e)
            Uri.fromFile(imageFile)
        }
    }

    fun normalizeImage(context: Context, uri: Uri): Uri {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL

            val rotationDegrees = getRotationDegrees(orientation)
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            saveNormalizedBitmap(bitmap, rotationDegrees, "gallery_${System.currentTimeMillis()}.jpg")
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to normalize image from URI", e)
            uri
        }
    }

    private fun getRotationDegrees(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun saveNormalizedBitmap(bitmap: Bitmap, rotationDegrees: Int, fileName: String): Uri {
        return try {
            if (rotationDegrees == 0) {
                val tempFile = File(context.cacheDir, fileName)
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                Uri.fromFile(tempFile)
            } else {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                val normalizedFile = File(context.cacheDir, "normalized_$fileName")
                FileOutputStream(normalizedFile).use { out ->
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                val newExif = ExifInterface(normalizedFile.absolutePath)
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
                newExif.saveAttributes()
                Uri.fromFile(normalizedFile)
            }
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to save normalized bitmap", e)
            Uri.EMPTY
        }
    }
}
