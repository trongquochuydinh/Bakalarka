package cz.zcu.kiv.dinh.ml.processors

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.util.Base64
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.Executors

/**
 * Abstraktní základní třída pro všechny ML procesory (Image Labeling, Text Recognition, Object Detection).
 * Obsahuje společnou logiku pro volání Google Cloud Vision API a práci s bitmapami.
 */
abstract class BaseMLProcessor {

    /**
     * Zpracuje obrázek daným modelem (cloud/offline) a vrátí výsledky jako seznam řetězců a dobu zpracování.
     */
    abstract fun processImage(
        context: Context,
        imageUri: android.net.Uri,
        onResult: (List<String>, Long) -> Unit
    )

    /**
     * Zpracuje obrázek pomocí Google Cloud Vision API.
     */
    abstract fun processWithCloudVisionAPI(
        context: Context,
        imageUri: android.net.Uri,
        onResult: (List<String>, Long) -> Unit
    )

    /**
     * Společná metoda pro práci s Google Cloud Vision API.
     * Provádí downsampling obrázku, konverzi do Base64 a odeslání požadavku.
     *
     * @param featureType typ požadované detekce
     * @param maxResults maximální počet požadovaných výsledků
     * @param parse funkce, která zpracuje JSON odpověď na výstupní seznam
     */
    protected fun processCloudRequest(
        context: Context,
        imageUri: android.net.Uri,
        featureType: String,
        maxResults: Int = 10,
        onResult: (List<String>, Long) -> Unit,
        parse: (response: JSONObject, imageWidth: Int, imageHeight: Int) -> List<String>
    ) {
        // Běh na pozadí
        Executors.newSingleThreadExecutor().execute {
            val startTime = System.currentTimeMillis()
            try {
                // 1) Získání rozměrů obrázku
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(imageUri).use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                bounds.inSampleSize = calculateInSampleSize(bounds, 1200, 1200)
                bounds.inJustDecodeBounds = false

                // 2) Načtení downsampled bitmapy
                val bitmap = context.contentResolver.openInputStream(imageUri).use {
                    BitmapFactory.decodeStream(it, null, bounds)
                } ?: throw IOException("Failed to decode bitmap")

                // 3) Komprese a kódování do Base64
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                // 4) Vytvoření a odeslání požadavku
                val jsonBody = cz.zcu.kiv.dinh.ml.utils.CloudVisionUtils.createRequestPayload(base64, featureType, maxResults)
                val request = cz.zcu.kiv.dinh.ml.utils.CloudVisionUtils.buildRequest(jsonBody)

                OkHttpClient().newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        notifyEmpty(context, onResult, System.currentTimeMillis() - startTime)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val elapsed = System.currentTimeMillis() - startTime
                        if (!response.isSuccessful) {
                            notifyEmpty(context, onResult, elapsed)
                            return
                        }
                        try {
                            val json = JSONObject(response.body?.string() ?: "")
                            val results = parse(json, bitmap.width, bitmap.height)
                            Handler(Looper.getMainLooper()).post {
                                onResult(results, elapsed)
                                Toast.makeText(context, "$featureType (cloud) done", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Throwable) {
                            notifyEmpty(context, onResult, elapsed)
                        }
                    }
                })
            } catch (_: Throwable) {
                notifyEmpty(context, onResult, System.currentTimeMillis() - startTime)
            }
        }
    }

    /** Výpočet faktoru zmenšení obrázku */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (h, w) = options.outHeight to options.outWidth
        var inSample = 1
        if (h > reqHeight || w > reqWidth) {
            val halfH = h / 2
            val halfW = w / 2
            while (halfH / inSample >= reqHeight && halfW / inSample >= reqWidth) {
                inSample *= 2
            }
        }
        return inSample
    }

    /** Fallback metoda při chybě – vrací prázdný výsledek a zobrazí Toast */
    private fun notifyEmpty(context: Context, onResult: (List<String>, Long) -> Unit, processingTime: Long) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "No cloud results", Toast.LENGTH_SHORT).show()
            onResult(emptyList(), processingTime)
        }
    }
}
