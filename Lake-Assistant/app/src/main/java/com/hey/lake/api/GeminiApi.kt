package com.hey.lake.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.hey.lake.BuildConfig
import com.hey.lake.MyApplication
import com.hey.lake.utilities.ApiKeyManager
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.TextPart
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.hey.lake.utilities.NetworkConnectivityManager
import com.hey.lake.utilities.NetworkNotifier
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * FIXED VERSION: Refactored GeminiApi as a singleton object.
 * Now properly handles both proxy and direct API calls.
 * When proxy is not configured, it uses the direct Gemini API.
 */
object GeminiApi {
    private val proxyUrl: String = BuildConfig.GCLOUD_PROXY_URL
    private val proxyKey: String = BuildConfig.GCLOUD_PROXY_URL_KEY
    
    // Direct Gemini API endpoint
    private val DIRECT_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    val db = Firebase.firestore


    suspend fun generateContent(
        chat: List<Pair<String, List<Any>>>,
        images: List<Bitmap> = emptyList(),
        modelName: String = "gemini-2.5-flash",
        maxRetry: Int = 4,
        context: Context? = null
    ): String? {
        // Network check before making any calls
        try {
            val appCtx = context ?: MyApplication.appContext
            val isOnline = NetworkConnectivityManager(appCtx).isNetworkAvailable()
            if (!isOnline) {
                Log.e("GeminiApi", "No internet connection. Skipping generateContent call.")
                NetworkNotifier.notifyOffline()
                return null
            }
        } catch (e: Exception) {
            Log.e("GeminiApi", "Network check failed, assuming offline. ${e.message}")
            return null
        }
        
        // Check if proxy is configured
        val useProxy = !proxyUrl.isNullOrBlank() && !proxyKey.isNullOrBlank()
        Log.d("GeminiApi", "API Mode: ${if (useProxy) "Proxy" else "Direct"}")
        
        // Extract the last user prompt text for logging purposes.
        val lastUserPrompt = chat.lastOrNull { it.first == "user" }
            ?.second
            ?.filterIsInstance<TextPart>()
            ?.joinToString(separator = "\n") { it.text } ?: "No text prompt found"

        var attempts = 0
        while (attempts < maxRetry) {
            // Get a new API key for each attempt
            val currentApiKey = ApiKeyManager.getNextKey()
            Log.d("GeminiApi", "=== GEMINI API REQUEST (Attempt ${attempts + 1}) ===")
            Log.d("GeminiApi", "Using API key ending in: ...${currentApiKey.takeLast(4)}")
            Log.d("GeminiApi", "Model: $modelName")

            val attemptStartTime = System.currentTimeMillis()

            try {
                val response = if (useProxy) {
                    // Use proxy
                    val payload = buildProxyPayload(chat, modelName)
                    Log.d("GeminiApi", "Proxy Payload: ${payload.toString().take(500)}...")
                    
                    val request = Request.Builder()
                        .url(proxyUrl)
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-API-Key", proxyKey)
                        .build()

                    client.newCall(request).execute()
                } else {
                    // Use direct API
                    val payload = buildDirectPayload(chat)
                    Log.d("GeminiApi", "Direct API Payload: ${payload.toString().take(500)}...")
                    
                    val url = "$DIRECT_API_BASE/$modelName:generateContent?key=$currentApiKey"
                    val request = Request.Builder()
                        .url(url)
                        .post(payload.toString().toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .build()

                    client.newCall(request).execute()
                }

                response.use {
                    val responseEndTime = System.currentTimeMillis()
                    val requestTime = responseEndTime - attemptStartTime
                    val responseBody = response.body?.string()

                    Log.d("GeminiApi", "=== GEMINI API RESPONSE (Attempt ${attempts + 1}) ===")
                    Log.d("GeminiApi", "HTTP Status: ${response.code}")
                    Log.d("GeminiApi", "Request time: ${requestTime}ms")

                    if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                        Log.e("GeminiApi", "API call failed with HTTP ${response.code}. Response: $responseBody")
                        throw Exception("API Error ${response.code}: $responseBody")
                    }

                    // Parse response based on mode
                    val parsedResponse = if (useProxy) {
                        parseProxyResponse(responseBody)
                    } else {
                        parseDirectResponse(responseBody)
                    }

                    if (parsedResponse.isNullOrBlank()) {
                        Log.e("GeminiApi", "Failed to parse response. Body: $responseBody")
                        throw Exception("Failed to parse response")
                    }

                    val logEntry = createLogEntry(
                        attempt = attempts + 1,
                        modelName = modelName,
                        prompt = lastUserPrompt,
                        imagesCount = images.size,
                        payload = responseBody ?: "",
                        responseCode = response.code,
                        responseBody = responseBody,
                        responseTime = requestTime,
                        totalTime = requestTime
                    )
                    saveLogToFile(MyApplication.appContext, logEntry)
                    
                    val logData = createLogDataMap(
                        attempt = attempts + 1,
                        modelName = modelName,
                        prompt = lastUserPrompt,
                        imagesCount = images.size,
                        responseCode = response.code,
                        responseTime = requestTime,
                        totalTime = requestTime,
                        responseBody = responseBody,
                        status = "pass",
                    )
                    logToFirestore(logData)

                    return parsedResponse
                }
            } catch (e: Exception) {
                val attemptEndTime = System.currentTimeMillis()
                val totalAttemptTime = attemptEndTime - attemptStartTime

                Log.e("GeminiApi", "=== GEMINI API ERROR (Attempt ${attempts + 1}) ===", e)

                val logEntry = createLogEntry(
                    attempt = attempts + 1,
                    modelName = modelName,
                    prompt = lastUserPrompt,
                    imagesCount = images.size,
                    payload = "",
                    responseCode = null,
                    responseBody = null,
                    responseTime = 0,
                    totalTime = totalAttemptTime,
                    error = e.message
                )
                saveLogToFile(MyApplication.appContext, logEntry)
                
                val logData = createLogDataMap(
                    attempt = attempts + 1,
                    modelName = modelName,
                    prompt = lastUserPrompt,
                    imagesCount = images.size,
                    responseCode = null,
                    responseTime = 0,
                    totalTime = totalAttemptTime,
                    status = "error",
                    responseBody = null,
                    error = e.message
                )
                logToFirestore(logData)

                attempts++
                if (attempts < maxRetry) {
                    val delayTime = 1000L * attempts
                    Log.d("GeminiApi", "Retrying in ${delayTime}ms...")
                    delay(delayTime)
                } else {
                    Log.e("GeminiApi", "Request failed after all ${maxRetry} retries.")
                    return null
                }
            }
        }
        return null
    }

    /**
     * Builds payload for proxy server
     */
    private fun buildProxyPayload(chat: List<Pair<String, List<Any>>>, modelName: String): JSONObject {
        val rootObject = JSONObject()
        rootObject.put("modelName", modelName)

        val messagesArray = JSONArray()
        chat.forEach { (role, parts) ->
            val messageObject = JSONObject()
            messageObject.put("role", role.lowercase())

            val jsonParts = JSONArray()
            parts.forEach { part ->
                when (part) {
                    is TextPart -> {
                        val partObject = JSONObject().put("text", part.text)
                        jsonParts.put(partObject)
                    }
                    is ImagePart -> {
                        Log.w("GeminiApi", "ImagePart found but skipped. The proxy payload format does not support images.")
                    }
                }
            }

            if (jsonParts.length() > 0) {
                messageObject.put("parts", jsonParts)
                messagesArray.put(messageObject)
            }
        }

        rootObject.put("messages", messagesArray)
        return rootObject
    }

    /**
     * Builds payload for direct Gemini API
     */
    private fun buildDirectPayload(chat: List<Pair<String, List<Any>>>): JSONObject {
        val rootObject = JSONObject()
        val contentsArray = JSONArray()

        chat.forEach { (role, parts) ->
            val contentObject = JSONObject()
            contentObject.put("role", if (role == "model") "model" else "user")

            val partsArray = JSONArray()
            parts.forEach { part ->
                when (part) {
                    is TextPart -> {
                        val partObject = JSONObject().put("text", part.text)
                        partsArray.put(partObject)
                    }
                    is ImagePart -> {
                        Log.w("GeminiApi", "ImagePart found but skipped in direct API call.")
                    }
                }
            }

            if (partsArray.length() > 0) {
                contentObject.put("parts", partsArray)
                contentsArray.put(contentObject)
            }
        }

        rootObject.put("contents", contentsArray)
        return rootObject
    }

    /**
     * Parses response from proxy
     */
    private fun parseProxyResponse(responseBody: String): String? {
        return try {
            val json = JSONObject(responseBody)
            if (json.has("text")) {
                return json.getString("text")
            }
            if (!json.has("candidates")) {
                Log.w("GeminiApi", "Proxy response has no 'candidates'. Full response: $responseBody")
                return null
            }
            val candidates = json.getJSONArray("candidates")
            if (candidates.length() == 0) {
                return null
            }
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            Log.e("GeminiApi", "Failed to parse proxy response: $responseBody", e)
            responseBody
        }
    }

    /**
     * Parses response from direct Gemini API
     */
    private fun parseDirectResponse(responseBody: String): String? {
        return try {
            val json = JSONObject(responseBody)
            
            // Check for error
            if (json.has("error")) {
                val error = json.getJSONObject("error")
                val errorMsg = error.optString("message", "Unknown error")
                Log.e("GeminiApi", "API Error: $errorMsg")
                return null
            }
            
            // Parse standard Gemini response
            if (!json.has("candidates")) {
                Log.w("GeminiApi", "API response has no 'candidates'. It was likely blocked. Full response: $responseBody")
                return null
            }
            
            val candidates = json.getJSONArray("candidates")
            if (candidates.length() == 0) {
                Log.w("GeminiApi", "API response has an empty 'candidates' array.")
                return null
            }
            
            val firstCandidate = candidates.getJSONObject(0)
            if (!firstCandidate.has("content")) {
                Log.w("GeminiApi", "First candidate has no 'content' object.")
                return null
            }
            
            val content = firstCandidate.getJSONObject("content")
            if (!content.has("parts")) {
                Log.w("GeminiApi", "Content object has no 'parts' array.")
                return null
            }
            
            val parts = content.getJSONArray("parts")
            if (parts.length() == 0) {
                Log.w("GeminiApi", "Parts array is empty.")
                return null
            }
            
            parts.getJSONObject(0).getString("text")
        } catch (e: Exception) {
            Log.e("GeminiApi", "Failed to parse direct API response: $responseBody", e)
            null
        }
    }

    private fun saveLogToFile(context: Context, logEntry: String) {
        try {
            val logDir = File(context.filesDir, "gemini_logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            val logFile = File(logDir, "gemini_api_log.txt")

            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
            }
            Log.d("GeminiApi", "Log entry saved to: ${logFile.absolutePath}")

        } catch (e: Exception) {
            Log.e("GeminiApi", "Failed to save log to file", e)
        }
    }
    
    private fun logToFirestore(logData: Map<String, Any?>) {
        try {
            db.collection("gemini_api_logs")
                .add(logData)
                .addOnSuccessListener { documentReference ->
                    Log.d("GeminiApi", "Log added to Firestore with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("GeminiApi", "Failed to add log to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e("GeminiApi", "Failed to log to Firestore", e)
        }
    }

    private fun createLogEntry(
        attempt: Int,
        modelName: String,
        prompt: String,
        imagesCount: Int,
        payload: String,
        responseCode: Int?,
        responseBody: String?,
        responseTime: Long,
        totalTime: Long,
        error: String? = null
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val statusLine = if (error != null) "ERROR: $error" else "SUCCESS (HTTP $responseCode)"
        
        return """
            |==========================================
            |Timestamp: $timestamp
            |Attempt: $attempt
            |Model: $modelName
            |Prompt: ${prompt.take(200)}...
            |Images: $imagesCount
            |Status: $statusLine
            |Response Time: ${responseTime}ms
            |Total Time: ${totalTime}ms
            |${if (responseBody != null) "Response: ${responseBody.take(500)}..." else ""}
            |==========================================
            |
        """.trimMargin()
    }

    private fun createLogDataMap(
        attempt: Int,
        modelName: String,
        prompt: String,
        imagesCount: Int,
        responseCode: Int?,
        responseTime: Long,
        totalTime: Long,
        responseBody: String?,
        status: String,
        error: String? = null
    ): Map<String, Any?> {
        return mapOf(
            "timestamp" to FieldValue.serverTimestamp(),
            "attempt" to attempt,
            "modelName" to modelName,
            "prompt" to prompt.take(500),
            "imagesCount" to imagesCount,
            "responseCode" to responseCode,
            "responseTime" to responseTime,
            "totalTime" to totalTime,
            "status" to status,
            "error" to error,
            "responseBody" to responseBody?.take(1000)
        )
    }
}
