package com.hey.lake.utilities

import android.content.Context
import com.hey.lake.api.OfflineTTSVoice

/**
 * Manages voice preferences for offline TTS (PiperTTS)
 */
object VoicePreferenceManager {
    private const val PREFS_NAME = "LakeSettings"
    private const val KEY_SELECTED_VOICE = "selected_voice_offline"

    /**
     * Get the selected offline TTS voice
     */
    fun getSelectedVoice(context: Context): OfflineTTSVoice {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Default to British English female voice
        val selectedVoiceName = sharedPreferences.getString(
            KEY_SELECTED_VOICE, 
            OfflineTTSVoice.EN_GB_CORI_MEDIUM.name
        )
        
        return try {
            OfflineTTSVoice.valueOf(selectedVoiceName ?: OfflineTTSVoice.EN_GB_CORI_MEDIUM.name)
        } catch (e: Exception) {
            OfflineTTSVoice.EN_GB_CORI_MEDIUM
        }
    }

    /**
     * Save the selected offline TTS voice
     */
    fun saveSelectedVoice(context: Context, voice: OfflineTTSVoice) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(KEY_SELECTED_VOICE, voice.name)
            .apply()
    }
}
