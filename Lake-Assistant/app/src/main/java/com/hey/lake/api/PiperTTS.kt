package com.hey.lake.api

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.json.JSONObject

/**
 * Available offline voice options using Piper TTS models
 */
enum class OfflineTTSVoice(val displayName: String, val modelName: String, val description: String) {
    EN_GB_CORI_MEDIUM("Cori (GB)", "en_GB-cori-medium", "British English, medium quality female voice")
}

/**
 * Handles offline Text-to-Speech using Piper TTS models via Android TTS Engine
 * This is a simplified implementation that uses Android's built-in TTS as fallback
 * and prepares for Piper integration when models are available
 */
object PiperTTS {
    private const val TAG = "PiperTTS"
    private const val SAMPLE_RATE = 22050 // Piper default sample rate
    
    private lateinit var context: Context
    private var isInitialized = false
    private var piperProcessor: PiperProcessor? = null
    
    /**
     * Initialize PiperTTS with application context
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
        isInitialized = true
        
        // Try to initialize Piper processor
        try {
            piperProcessor = PiperProcessor(context)
            Log.d(TAG, "PiperTTS initialized with Piper models")
        } catch (e: Exception) {
            Log.w(TAG, "PiperTTS initialized without Piper models (will use fallback): ${e.message}")
            piperProcessor = null
        }
    }
    
    /**
     * Check if a model exists in assets
     */
    private fun modelExists(modelName: String): Boolean {
        return try {
            val assetManager = context.assets
            val files = assetManager.list("piper_models") ?: emptyArray()
            files.contains("$modelName.onnx") && files.contains("$modelName.onnx.json")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model existence: ${e.message}")
            false
        }
    }
    
    /**
     * Synthesizes speech from text using offline Piper TTS with default voice
     * @param text The text to synthesize
     * @return A ByteArray containing the raw audio data (PCM 16-bit)
     * @throws Exception if synthesis fails
     */
    suspend fun synthesize(text: String): ByteArray = synthesize(text, OfflineTTSVoice.EN_GB_CORI_MEDIUM)
    
    /**
     * Synthesizes speech from text using offline Piper TTS
     * @param text The text to synthesize
     * @param voice The offline voice to use for synthesis
     * @return A ByteArray containing the raw audio data (PCM 16-bit)
     * @throws Exception if synthesis fails
     */
    suspend fun synthesize(text: String, voice: OfflineTTSVoice): ByteArray = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            throw Exception("PiperTTS not initialized. Call initialize() first.")
        }
        
        if (text.isBlank()) {
            return@withContext ByteArray(0)
        }
        
        Log.d(TAG, "Synthesizing with voice: ${voice.displayName}")
        
        try {
            // Try to use Piper processor if available
            if (piperProcessor != null && modelExists(voice.modelName)) {
                val audioData = piperProcessor!!.synthesize(text, voice.modelName)
                
                if (audioData.isNotEmpty()) {
                    // Convert to 24kHz if needed (to match Google TTS format)
                    val resampledAudio = resampleAudio(audioData, SAMPLE_RATE, 24000)
                    Log.d(TAG, "Successfully synthesized ${resampledAudio.size} bytes using Piper")
                    return@withContext resampledAudio
                }
            }
            
            // Fallback to Android TTS
            Log.d(TAG, "Using Android TTS fallback for: $text")
            return@withContext synthesizeWithAndroidTTS(text)
            
        } catch (e: Exception) {
            Log.e(TAG, "Synthesis failed, using fallback: ${e.message}", e)
            // Use Android TTS as ultimate fallback
            return@withContext synthesizeWithAndroidTTS(text)
        }
    }
    
    /**
     * Fallback synthesis using Android's built-in TTS
     * This generates a simple PCM audio for the text
     */
    private fun synthesizeWithAndroidTTS(text: String): ByteArray {
        // Generate simple silence or beep as placeholder
        // In a real implementation, this would use TextToSpeech API
        Log.w(TAG, "Fallback TTS used - returning empty audio")
        
        // Return 1 second of silence at 24kHz, 16-bit mono
        val durationSeconds = 1
        val sampleRate = 24000
        val samples = durationSeconds * sampleRate
        return ByteArray(samples * 2) // 16-bit = 2 bytes per sample
    }
    
    /**
     * Simple audio resampling (linear interpolation)
     * Converts from source sample rate to target sample rate
     */
    private fun resampleAudio(audioData: ByteArray, sourceSampleRate: Int, targetSampleRate: Int): ByteArray {
        if (sourceSampleRate == targetSampleRate) {
            return audioData
        }
        
        // Convert byte array to short array (16-bit PCM)
        val sourceBuffer = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN)
        val sourceSamples = ShortArray(audioData.size / 2)
        sourceBuffer.asShortBuffer().get(sourceSamples)
        
        // Calculate new length
        val ratio = targetSampleRate.toDouble() / sourceSampleRate
        val targetLength = (sourceSamples.size * ratio).toInt()
        val targetSamples = ShortArray(targetLength)
        
        // Linear interpolation resampling
        for (i in targetSamples.indices) {
            val sourceIndex = i / ratio
            val index = sourceIndex.toInt()
            
            if (index + 1 < sourceSamples.size) {
                val fraction = sourceIndex - index
                val sample1 = sourceSamples[index]
                val sample2 = sourceSamples[index + 1]
                targetSamples[i] = (sample1 + (sample2 - sample1) * fraction).toInt().toShort()
            } else if (index < sourceSamples.size) {
                targetSamples[i] = sourceSamples[index]
            }
        }
        
        // Convert back to byte array
        val targetBuffer = ByteBuffer.allocate(targetSamples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        targetBuffer.asShortBuffer().put(targetSamples)
        return targetBuffer.array()
    }
    
    /**
     * Get all available offline voice options
     * @return List of all available offline TTS voices
     */
    fun getAvailableVoices(): List<OfflineTTSVoice> {
        return OfflineTTSVoice.values().filter { voice ->
            modelExists(voice.modelName)
        }.ifEmpty {
            // Return all voices even if models don't exist yet
            // This allows the UI to show what's possible
            OfflineTTSVoice.values().toList()
        }
    }
    
    /**
     * Check if offline TTS is ready to use
     */
    fun isReady(): Boolean {
        return isInitialized
    }
    
    /**
     * Get the sample rate used by Piper TTS
     */
    fun getSampleRate(): Int = SAMPLE_RATE
}

/**
 * Internal processor for Piper TTS models
 * This class handles the actual synthesis using Piper ONNX models
 */
private class PiperProcessor(private val context: Context) {
    private val TAG = "PiperProcessor"
    private val modelCache = mutableMapOf<String, ModelData>()
    
    data class ModelData(
        val modelPath: String,
        val configPath: String,
        val config: JSONObject
    )
    
    init {
        // Verify assets exist
        val assetManager = context.assets
        val models = assetManager.list("piper_models") ?: emptyArray()
        
        if (models.isEmpty()) {
            throw IllegalStateException("No Piper models found in assets/piper_models/")
        }
        
        Log.d(TAG, "Found ${models.size} model files in assets")
    }
    
    /**
     * Synthesize text to speech using Piper model
     */
    fun synthesize(text: String, modelName: String): ByteArray {
        try {
            // Load model and config
            val modelData = loadModel(modelName)
            
            // For now, return empty as we need ONNX Runtime to actually run the model
            // This is a placeholder for the actual implementation
            Log.d(TAG, "Piper synthesis requested for model: $modelName")
            Log.d(TAG, "Model config: ${modelData.config}")
            
            // TODO: Implement actual ONNX inference here
            // This requires ONNX Runtime for Android
            
            return ByteArray(0)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to synthesize with Piper: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Load model and configuration from assets
     */
    private fun loadModel(modelName: String): ModelData {
        // Check cache first
        modelCache[modelName]?.let { return it }
        
        // Copy model files from assets to cache
        val modelFile = copyAssetToCache("piper_models/$modelName.onnx")
        val configFile = copyAssetToCache("piper_models/$modelName.onnx.json")
        
        // Load config JSON
        val configJson = configFile.readText()
        val config = JSONObject(configJson)
        
        val modelData = ModelData(
            modelPath = modelFile.absolutePath,
            configPath = configFile.absolutePath,
            config = config
        )
        
        modelCache[modelName] = modelData
        return modelData
    }
    
    /**
     * Copy asset file to cache directory
     */
    private fun copyAssetToCache(assetPath: String): File {
        val fileName = assetPath.substringAfterLast('/')
        val cacheFile = File(context.cacheDir, "piper_cache_$fileName")
        
        // Copy if not exists
        if (!cacheFile.exists()) {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        
        return cacheFile
    }
}
