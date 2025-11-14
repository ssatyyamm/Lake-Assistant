package com.hey.lake

import android.app.Application
import android.content.Context
import com.hey.lake.utilities.Logger
import com.hey.lake.intents.IntentRegistry
import com.hey.lake.api.PiperTTS
import com.hey.lake.intents.impl.DialIntent
import com.hey.lake.intents.impl.EmailComposeIntent
import com.hey.lake.intents.impl.ShareTextIntent
import com.hey.lake.intents.impl.ViewUrlIntent
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MyApplication - Modified to remove billing functionality
 * All features are now free and unlimited
 */
class MyApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        lateinit var appContext: Context
            private set

        // Removed billingClient - no longer needed
        
        // Always true since billing is removed
        private val _isBillingClientReady = MutableStateFlow(true)
        val isBillingClientReady: StateFlow<Boolean> = _isBillingClientReady.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        // Initialize Firebase Remote Config
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 1L else 3L
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // Removed billing client initialization - no longer needed
        Logger.d("MyApplication", "App initialized in free/unlimited mode - no billing required")

        // Initialize offline TTS
        PiperTTS.initialize(this)
        Logger.d("MyApplication", "PiperTTS initialized for offline speech synthesis")
        
        // Initialize intent registry
        IntentRegistry.register(DialIntent())
        IntentRegistry.register(ViewUrlIntent())
        IntentRegistry.register(ShareTextIntent())
        IntentRegistry.register(EmailComposeIntent())
        IntentRegistry.init(this)
    }
}
