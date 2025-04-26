package cz.zcu.kiv.dinh.ml.utils

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

import cz.zcu.kiv.dinh.ml.configs.TextRecognitionConfig

object CloudVisionUtils {
    // Always consider storing your API key securely via build configs or secured storage.
    private const val API_KEY = "YOUR_API_KEY"
    private const val BASE_URL = "https://vision.googleapis.com/v1/images:annotate"

    /**
     * Creates a standardized JSON payload for the Cloud Vision API request.
     *
     * @param base64Image the Base64-encoded image string.
     * @param featureType one of: "LABEL_DETECTION", "TEXT_DETECTION", "OBJECT_LOCALIZATION".
     * @param maxResults maximum number of results.
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
     * Builds an OkHttp Request using the provided JSON payload and the stored API key.
     */
    fun buildRequest(jsonBody: String): Request {
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        return Request.Builder()
            .url("$BASE_URL?key=$API_KEY")
            .post(requestBody)
            .build()
    }

    /**
     * Parses a Cloud Vision label detection response and returns a list of formatted strings.
     * Format: "Label (Confidence%)".
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
     * Parses a Cloud Vision object localization response and returns a list of formatted strings.
     * Format: "ObjectName (Confidence%) - Box: [left, top, right, bottom]".
     */
    fun parseObjectLocalizationResponse(response: JSONObject, minConfidence: Int, imageWidth: Int, imageHeight: Int): List<String> {
        val results = mutableListOf<String>()

        // ✅ DEBUG: Print entire raw API response
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
     * Parses a Cloud Vision text detection response and returns a list of formatted strings.
     * Note: Typically, textAnnotations[0] contains the full text; subsequent elements represent parts.
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

        for (i in 1 until annotations.length()) { // začínáme od 1
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