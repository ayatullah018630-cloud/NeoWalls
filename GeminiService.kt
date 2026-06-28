package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    data class AICreationResult(
        val title: String,
        val colorHex: String,
        val category: String,
        val expandedPrompt: String,
        val imageUrl: String
    )

    suspend fun generateWallpaperMetadata(userPrompt: String): AICreationResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is missing or default. Falling back to local synthesis.")
            return@withContext getFallbackResult(userPrompt)
        }

        val systemInstruction = """
            You are NeoWalls AI, an elite AI Wallpaper designer.
            The user will provide a simple theme or prompt. You must expand this into a stunning wallpaper concept and return a JSON object.
            Do not include any markdown format like ```json ... ``` in your output. Return ONLY the raw JSON string.
            The JSON structure must be exactly:
            {
              "title": "A short, elegant, creative title for the wallpaper",
              "colorHex": "A vibrant hexadecimal color matching the theme (e.g. #00E5FF)",
              "category": "One of the following exact categories: AMOLED, Minimal, 4K Ultra HD, Nature, Space & Galaxy, Cars & Supercars, Cyberpunk, Neon, Animals, Abstract",
              "expandedPrompt": "A highly detailed art-director prompt describing the composition, lighting (e.g. dramatic, volumetric, neon glows), rendering engine, artistic medium (e.g., 3D render, luxury dark matte, oil painting), and visual rhythm."
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", userPrompt)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP error: ${response.code} ${response.message}")
                    return@withContext getFallbackResult(userPrompt)
                }

                val responseBody = response.body?.string() ?: return@withContext getFallbackResult(userPrompt)
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textResponse = parts?.optJSONObject(0)?.optString("text")?.trim()

                if (!textResponse.isNullOrEmpty()) {
                    val parsed = JSONObject(textResponse)
                    val title = parsed.optString("title", "AI Synthesized Canvas")
                    val colorHex = parsed.optString("colorHex", "#7E57C2")
                    val category = parsed.optString("category", "Abstract")
                    val expandedPrompt = parsed.optString("expandedPrompt", userPrompt)
                    
                    // Construct a beautiful dynamic Unsplash search query using prompt keywords
                    val keywords = userPrompt.split(" ").take(3).joinToString(",") { it.lowercase() }
                    val imageUrl = "https://images.unsplash.com/featured/1080x1920/?$keywords,wallpaper"

                    AICreationResult(title, colorHex, category, expandedPrompt, imageUrl)
                } else {
                    getFallbackResult(userPrompt)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed, using local fallback", e)
            getFallbackResult(userPrompt)
        }
    }

    private fun getFallbackResult(prompt: String): AICreationResult {
        val cleanPrompt = prompt.trim()
        val title = if (cleanPrompt.length > 20) cleanPrompt.take(20) + "..." else cleanPrompt
        
        // Pick a dynamic category based on keyword matching
        val lower = cleanPrompt.lowercase()
        val category = when {
            lower.contains("amoled") || lower.contains("black") || lower.contains("dark") -> "AMOLED"
            lower.contains("minimal") || lower.contains("simple") -> "Minimal"
            lower.contains("car") || lower.contains("porsche") || lower.contains("race") -> "Cars & Supercars"
            lower.contains("space") || lower.contains("galaxy") || lower.contains("stars") -> "Space & Galaxy"
            lower.contains("cyber") || lower.contains("punk") -> "Cyberpunk"
            lower.contains("neon") || lower.contains("glow") -> "Neon"
            lower.contains("cat") || lower.contains("dog") || lower.contains("animal") -> "Animals"
            lower.contains("nature") || lower.contains("mountain") || lower.contains("sunset") -> "Nature"
            else -> "Abstract"
        }

        // Map categories to glowing hex colors
        val colorHex = when (category) {
            "AMOLED" -> "#0A0A0C"
            "Minimal" -> "#E0E0E0"
            "Cars & Supercars" -> "#FF3D00"
            "Space & Galaxy" -> "#3F51B5"
            "Cyberpunk" -> "#FF007F"
            "Neon" -> "#00E5FF"
            "Animals" -> "#FFB300"
            "Nature" -> "#4CAF50"
            else -> "#7E57C2"
        }

        val keywords = cleanPrompt.split(" ").take(3).joinToString(",") { it.lowercase() }
        val imageUrl = "https://images.unsplash.com/featured/1080x1920/?$keywords,wallpaper"

        return AICreationResult(
            title = title.capitalize(),
            colorHex = colorHex,
            category = category,
            expandedPrompt = "A beautifully synthesized digital composition capturing the essence of '$cleanPrompt'. High dynamic contrast, elegant geometries, and vibrant atmospheric perspective.",
            imageUrl = imageUrl
        )
    }
}
