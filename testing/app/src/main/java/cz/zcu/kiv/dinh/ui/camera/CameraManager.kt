package cz.zcu.kiv.dinh.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

/**
 * Třída CameraManager zajišťuje inicializaci, spuštění, zastavení a použití kamery
 * včetně pořizování fotografií a jejich následné normalizace.
 */
class CameraManager(private val context: Context) {

    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null

    /**
     * Inicializuje kameru, nastaví náhled a připraví snímání.
     * @return náhledový prvek PreviewView, který lze vložit do UI.
     */
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

    /**
     * Zastaví kameru a uvolní prostředky.
     */
    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    /**
     * Pořídí fotografii, uloží ji do souboru a zavolá zpětnou vazbu s výsledným URI.
     * @param executor vlákno, ve kterém se provádí snímání.
     * @param onImageCaptured zpětná vazba s výsledným URI nebo null při chybě.
     */
    fun takePhoto(
        executor: ExecutorService,
        onImageCaptured: (Uri?) -> Unit
    ) {
        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
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

    /**
     * Normalizuje obrázek ze souboru – provede downsampling a otočení dle EXIF.
     * @param imageFile soubor s obrázkem.
     * @return URI k výslednému normalizovanému obrázku.
     */
    private fun normalizeImage(imageFile: File): Uri {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imageFile.absolutePath, opts)

            val targetDim = 1024
            opts.apply {
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(this, targetDim, targetDim)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath, opts)
                ?: return Uri.fromFile(imageFile)

            val ori = ExifInterface(imageFile.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val rotationDegrees = getRotationDegrees(ori)

            saveNormalizedBitmap(bitmap, rotationDegrees, imageFile.name)
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to normalize image", e)
            Uri.fromFile(imageFile)
        }
    }

    /**
     * Alternativní metoda normalizace pro obrázky z galerie (z Uri).
     * @param context kontext aplikace.
     * @param uri URI původního obrázku.
     * @return URI normalizovaného obrázku.
     */
    fun normalizeImage(context: Context, uri: Uri): Uri {
        return try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            }

            val targetDim = 1024
            opts.apply {
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(this, targetDim, targetDim)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            } ?: return uri

            val ori = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
            val rotationDegrees = getRotationDegrees(ori)

            saveNormalizedBitmap(bitmap, rotationDegrees, "gallery_${System.currentTimeMillis()}.jpg")
        } catch (e: Exception) {
            Log.e("CameraManager", "Failed to normalize image from URI", e)
            uri
        }
    }

    /**
     * Vypočítá vhodný faktor pro zmenšení obrázku.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfH = height / 2
            val halfW = width / 2
            while (halfH / inSampleSize >= reqHeight && halfW / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Převede EXIF orientaci na počet stupňů otočení.
     */
    private fun getRotationDegrees(orientation: Int): Int {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90  -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else                                 -> 0
        }
    }

    /**
     * Uloží bitmapu na disk, včetně případné rotace.
     * @return URI k výslednému souboru.
     */
    private fun saveNormalizedBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int,
        fileName: String
    ): Uri {
        return try {
            if (rotationDegrees == 0) {
                val tempFile = File(context.cacheDir, fileName)
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                Uri.fromFile(tempFile)
            } else {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                val normalizedFile = File(context.cacheDir, "normalized_$fileName")
                FileOutputStream(normalizedFile).use { out ->
                    rotated.compress(Bitmap.CompressFormat.JPEG, 90, out)
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
