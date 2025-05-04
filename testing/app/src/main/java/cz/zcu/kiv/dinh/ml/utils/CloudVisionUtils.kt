package cz.zcu.kiv.dinh.ml.utils

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

import cz.zcu.kiv.dinh.ml.configs.TextRecognitionConfig


/**
 * Pomocné metody pro komunikaci s Google Cloud Vision API.
 * Zahrnuje vytváření požadavků a parsování odpovědí pro různé typy detekce.
 */
object CloudVisionUtils {
    // ⚠️ V produkční aplikaci je doporučeno uchovávat klíč bezpečně (např. v build configu).
    private const val API_KEY = "YOUR_API_KEY"
    private const val BASE_URL = "https://vision.googleapis.com/v1/images:annotate"

    /**
     * Vytvoří JSON payload pro Cloud Vision API požadavek.
     *
     * @param base64Image Obrázek ve formátu Base64
     * @param featureType Typ požadované detekce: "LABEL_DETECTION", "TEXT_DETECTION", "OBJECT_LOCALIZATION"
     * @param maxResults Maximální počet výsledků
     */
    fun createRequestPayload(base64Image: String, featureType: String, maxResults: Int = 10): String {
        return """
        {
          "requests": [
            {
              "image": {
                "content": "$base64Image"
              },
              "features": [
                {
                  "type": "$featureType",
                      "maxResults": $maxResults
                }
              ]
            }
          ]
        }
        """.trimIndent()
    }

    /**
     * Sestaví HTTP požadavek pro Cloud Vision API pomocí zadaného JSON těla.
     */
    fun buildRequest(jsonBody: String): Request {
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        return Request.Builder()
            .url("$BASE_URL?key=$API_KEY")
            .post(requestBody)
            .build()
    }

    /**
     * Zpracuje odpověď pro label detekci a vrátí seznam ve formátu "Label (Confidence%)".
     */
    fun parseLabelResponse(response: JSONObject, minConfidence: Int): List<String> {
        val results = mutableListOf<String>()
        val responses = response.optJSONArray("responses")
        val annotations = responses?.optJSONObject(0)?.optJSONArray("labelAnnotations") ?: JSONArray()
        for (i in 0 until annotations.length()) {
            val annotation = annotations.getJSONObject(i)
            val description = annotation.optString("description")
            val confidence = (annotation.optDouble("score") * 100).toInt()
            if (confidence >= minConfidence) {
                results.add("$description ($confidence%)")
            }
        }
        Log.d("CloudVision", "Parsed labels: $results")
        return results
    }

    /**
     * Zpracuje odpověď pro lokalizaci objektů a převede ji na čitelné výsledky s bounding boxem.
     */
    fun parseObjectLocalizationResponse(response: JSONObject, minConfidence: Int, imageWidth: Int, imageHeight: Int): List<String> {
        val results = mutableListOf<String>()

        // ✅ DEBUG: Výpis celé odpovědi
        Log.d("CloudVision", "Full response:\n${response.toString(2)}")

        val responses = response.optJSONArray("responses")
        val annotations = responses?.optJSONObject(0)?.optJSONArray("localizedObjectAnnotations") ?: JSONArray()
        for (i in 0 until annotations.length()) {
            val obj = annotations.getJSONObject(i)
            val name = obj.optString("name")
            val confidence = (obj.optDouble("score") * 100).toInt()
            if (confidence >= minConfidence) {
                val boundingPoly = obj.optJSONObject("boundingPoly")
                val vertices = boundingPoly?.optJSONArray("normalizedVertices")
                if (vertices != null && vertices.length() > 0) {
                    val xs = mutableListOf<Double>()
                    val ys = mutableListOf<Double>()
                    for (j in 0 until vertices.length()) {
                        val vertex = vertices.getJSONObject(j)
                        val x = vertex.optDouble("x", 0.0)
                        val y = vertex.optDouble("y", 0.0)
                        xs.add(x)
                        ys.add(y)
                    }
                    val left = (xs.minOrNull() ?: 0.0) * imageWidth
                    val top = (ys.minOrNull() ?: 0.0) * imageHeight
                    val right = (xs.maxOrNull() ?: 0.0) * imageWidth
                    val bottom = (ys.maxOrNull() ?: 0.0) * imageHeight
                    results.add("$name ($confidence%) - Box: [${left.toInt()}, ${top.toInt()}, ${right.toInt()}, ${bottom.toInt()}]")
                } else {
                    results.add("$name ($confidence%) - Box: [0, 0, 0, 0]")
                }
            }
        }

        Log.d("CloudVision", "Parsed objects: $results")
        return results
    }

    /**
     * Zpracuje odpověď z textového rozpoznávání a vytvoří seznam segmentů se souřadnicemi.
     */
    fun parseTextRecognitionResponse(response: JSONObject): List<String> {
        val results = mutableListOf<String>()
        val responses = response.optJSONArray("responses")
        val annotations = responses?.optJSONObject(0)?.optJSONArray("textAnnotations") ?: JSONArray()

        val mode = TextRecognitionConfig.segmentationMode.lowercase()
        val prefix = when (mode) {
            "block" -> "Block"
            "line" -> "Line"
            "word" -> "Word"
            "symbol" -> "Symbol"
            else -> "Block"
        }

        for (i in 1 until annotations.length()) { // přeskočíme první (celý text)
            val annotation = annotations.getJSONObject(i)
            val description = annotation.optString("description")
            val boundingPoly = annotation.optJSONObject("boundingPoly")
            val vertices = boundingPoly?.optJSONArray("vertices")

            val (left, top, right, bottom) = if (vertices != null && vertices.length() > 0) {
                val xs = mutableListOf<Int>()
                val ys = mutableListOf<Int>()
                for (j in 0 until vertices.length()) {
                    val vertex = vertices.getJSONObject(j)
                    xs.add(vertex.optInt("x", 0))
                    ys.add(vertex.optInt("y", 0))
                }
                listOf(
                    xs.minOrNull() ?: 0,
                    ys.minOrNull() ?: 0,
                    xs.maxOrNull() ?: 0,
                    ys.maxOrNull() ?: 0
                )
            } else {
                listOf(0, 0, 0, 0)
            }

            val shouldInclude = when {
                TextRecognitionConfig.highlightSymbol != null ->
                    TextRecognitionConfig.highlightSymbol!!.toString() in description
                TextRecognitionConfig.highlightWord != null ->
                    description.contains(TextRecognitionConfig.highlightWord!!, ignoreCase = true)
                else -> true
            }

            if (shouldInclude) {
                results.add("$prefix: $description ([${left}, ${top}, ${right}, ${bottom}])")
            }
        }

        Log.d("CloudVision", "Parsed text: $results")
        return results
    }
}