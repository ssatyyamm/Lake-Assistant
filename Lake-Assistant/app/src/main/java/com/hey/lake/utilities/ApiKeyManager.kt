package com.hey.lake.utilities

import com.hey.lake.BuildConfig
import java.util.concurrent.atomic.AtomicInteger

/**
 * A thread-safe, singleton object to manage and rotate a list of API keys.
 * This ensures that every part of the app gets the next key in the sequence.
 */
object ApiKeyManager {

    private val apiKeys: List<String> = if (BuildConfig.GEMINI_API_KEYS.isNotEmpty() && BuildConfig.GEMINI_API_KEYS != "YOUR_GEMINI_API_KEY_HERE") {
        BuildConfig.GEMINI_API_KEYS.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    } else {
        emptyList()
    }

    private val currentIndex = AtomicInteger(0)

    /**
     * Gets the next API key from the list in a circular, round-robin fashion.
     * @return The next API key as a String.
     */
    fun getNextKey(): String {
        if (apiKeys.isEmpty()) {
            throw IllegalStateException(
                "\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "  GEMINI API KEY NOT CONFIGURED!\n" +
                "════════════════════════════════════════════════════════════════\n" +
                "\n" +
                "Lake Assistant requires a Gemini API key to function.\n" +
                "\n" +
                "To fix this issue:\n" +
                "\n" +
                "1. Get a FREE Gemini API key from:\n" +
                "   https://makersuite.google.com/app/apikey\n" +
                "\n" +
                "2. Open the 'local.properties' file in your project root\n" +
                "\n" +
                "3. Replace YOUR_GEMINI_API_KEY_HERE with your actual key:\n" +
                "   GEMINI_API_KEYS=your_actual_api_key_here\n" +
                "\n" +
                "4. Rebuild and reinstall the app\n" +
                "\n" +
                "For multiple keys (better rate limiting), use commas:\n" +
                "   GEMINI_API_KEYS=key1,key2,key3\n" +
                "\n" +
                "════════════════════════════════════════════════════════════════\n"
            )
        }
        // Get the current index, then increment it for the next call.
        // The modulo operator (%) makes it loop back to 0 when it reaches the end.
        val index = currentIndex.getAndIncrement() % apiKeys.size
        return apiKeys[index]
    }
    
    /**
     * Check if API keys are properly configured
     */
    fun hasValidKeys(): Boolean {
        return apiKeys.isNotEmpty()
    }
}