package com.hey.lake

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hey.lake.v2.AgentService
import com.hey.lake.utilities.FreemiumManager
import com.hey.lake.utilities.Logger
import com.hey.lake.utilities.OnboardingManager
import com.hey.lake.utilities.PermissionManager
import com.hey.lake.utilities.UserIdManager
import com.hey.lake.utilities.UserProfileManager
import com.hey.lake.utilities.VideoAssetManager
import com.hey.lake.utilities.WakeWordManager
import com.hey.lake.utilities.LakeState
import com.hey.lake.utilities.LakeStateManager
import com.hey.lake.utilities.DeltaStateColorMapper
import com.hey.lake.views.DeltaSymbolView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * MainActivity - Modified to remove all subscription/billing functionality
 * All features are now free and unlimited
 */
class MainActivity : BaseNavigationActivity() {

    private lateinit var handler: Handler
    private lateinit var managePermissionsButton: TextView
    private lateinit var tvPermissionStatus: TextView
    private lateinit var userId: String
    private lateinit var permissionManager: PermissionManager
    private lateinit var wakeWordManager: WakeWordManager
    private lateinit var auth: FirebaseAuth
    private lateinit var tasksLeftTag: View
    private lateinit var freemiumManager: FreemiumManager
    private lateinit var wakeWordHelpLink: TextView
    private lateinit var increaseLimitsLink: TextView
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var requestRoleLauncher: ActivityResultLauncher<Intent>
    private lateinit var statusTextView: TextView
    private lateinit var permissionsTag: View
    private lateinit var permissionsStatusTag: TextView
    private lateinit var tasksLeftText: TextView
    private lateinit var deltaSymbol: DeltaSymbolView
    private lateinit var lakeStateManager: LakeStateManager
    private lateinit var stateChangeListener: (LakeState) -> Unit

    private lateinit var root: View
    companion object {
        const val ACTION_WAKE_WORD_FAILED = "com.hey.lake.WAKE_WORD_FAILED"
    }
    
    private val wakeWordFailureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_WAKE_WORD_FAILED) {
                Logger.d("MainActivity", "Received wake word failure broadcast.")
                updateUI()
                showWakeWordFailureDialog()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        val profileManager = UserProfileManager(this)

        if (currentUser == null || !profileManager.isProfileComplete()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        onboardingManager = OnboardingManager(this)
        if (!onboardingManager.isOnboardingCompleted()) {
            Logger.d("MainActivity", "User is logged in but onboarding not completed. Relaunching permissions stepper.")
            startActivity(Intent(this, OnboardingPermissionsActivity::class.java))
            finish()
            return
        }

        requestRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Set as default assistant successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Couldn't become default assistant. Opening settings…", Toast.LENGTH_SHORT).show()
                Logger.w("MainActivity", "Role request canceled or app not eligible.\n${explainAssistantEligibility()}")
                openAssistantPickerSettings()
            }
            showAssistantStatus(true)
        }

        setContentView(R.layout.activity_main_content)
        findViewById<TextView>(R.id.btn_set_default_assistant).setOnClickListener {
            startActivity(Intent(this, RoleRequestActivity::class.java))
        }
        updateDefaultAssistantButtonVisibility()

        handleIntent(intent)
        managePermissionsButton = findViewById(R.id.btn_manage_permissions)

        val userIdManager = UserIdManager(applicationContext)
        userId = userIdManager.getOrCreateUserId()
        increaseLimitsLink = findViewById(R.id.increase_limits_link)

        permissionManager = PermissionManager(this)
        permissionManager.initializePermissionLauncher()

        managePermissionsButton = findViewById(R.id.btn_manage_permissions)
        tasksLeftText = findViewById(R.id.tasks_left_tag_text)
        tasksLeftTag = findViewById(R.id.tasks_left_tag)
        tvPermissionStatus = findViewById(R.id.tv_permission_status)
        wakeWordHelpLink = findViewById(R.id.wakeWordHelpLink)
        statusTextView = findViewById(R.id.status_text)
        permissionsTag = findViewById(R.id.permissions_tag)
        permissionsStatusTag = findViewById(R.id.permissions_status_tag)
        deltaSymbol = findViewById(R.id.delta_symbol)
        freemiumManager = FreemiumManager()
        
        updateStatusText(LakeState.IDLE)
        initializeLakeStateManager()
        wakeWordManager = WakeWordManager(this, requestPermissionLauncher)
        handler = Handler(Looper.getMainLooper())
        setupClickListeners()
        
        // Provision user as pro automatically
        lifecycleScope.launch {
            freemiumManager.provisionUserIfNeeded()
        }
        
        lifecycleScope.launch {
            val videoUrl = "https://storage.googleapis.com/lake-app-assets/wake_word_demo.mp4"
            VideoAssetManager.getVideoFile(this@MainActivity, videoUrl)
        }
    }

    private fun openAssistantPickerSettings() {
        val specifics = listOf(
            Intent("android.settings.VOICE_INPUT_SETTINGS"),
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        )
        for (i in specifics) {
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i); return
            }
        }
        Toast.makeText(this, "Assistant settings not available on this device.", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showAssistantStatus(toast: Boolean = false) {
        val rm = getSystemService(RoleManager::class.java)
        val held = rm?.isRoleHeld(RoleManager.ROLE_ASSISTANT) == true
        val msg = if (held) "This app is the default assistant." else "This app is NOT the default assistant."
        if (toast) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Logger.d("MainActivity", msg)
    }

    private fun explainAssistantEligibility(): String {
        val pm = packageManager
        val pkg = packageName

        val assistIntent = Intent(Intent.ACTION_ASSIST).setPackage(pkg)
        val assistActivities = pm.queryIntentActivities(assistIntent, 0)

        val visIntent = Intent("android.service.voice.VoiceInteractionService").setPackage(pkg)
        val visServices = pm.queryIntentServices(visIntent, 0)

        return buildString {
            append("Assistant eligibility:\n")
            append("• ACTION_ASSIST activity: ${if (assistActivities.isNotEmpty()) "FOUND" else "NOT FOUND"}\n")
            append("• VoiceInteractionService: ${if (visServices.isNotEmpty()) "FOUND" else "NOT FOUND"}\n")
            append("Note: Many OEMs only list apps with a VoiceInteractionService as selectable assistants.\n")
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "com.hey.lake.WAKE_UP_LAKE") {
            Logger.d("MainActivity", "Wake up Lake shortcut activated!")
            startConversationalAgent()
        }
    }

    private fun startConversationalAgent() {
        if (!ConversationalAgentService.isRunning) {
            val serviceIntent = Intent(this, ConversationalAgentService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
            Toast.makeText(this, "Lake is waking up...", Toast.LENGTH_SHORT).show()
        } else {
            Logger.d("MainActivity", "ConversationalAgentService is already running.")
            Toast.makeText(this, "Lake is already awake!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun getContentLayoutId(): Int = R.layout.activity_main_content
    
    override fun getCurrentNavItem(): BaseNavigationActivity.NavItem = BaseNavigationActivity.NavItem.HOME

    private fun setupClickListeners() {
        managePermissionsButton.setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
        increaseLimitsLink.setOnClickListener {
            requestLimitIncrease()
        }

        wakeWordHelpLink.setOnClickListener {
            showWakeWordFailureDialog()
        }
        findViewById<TextView>(R.id.disclaimer_link).setOnClickListener {
            showDisclaimerDialog()
        }
        findViewById<TextView>(R.id.examples_link).setOnClickListener {
            showExamplesDialog()
        }
        
        // Add click listener to delta symbol
        deltaSymbol.setOnClickListener {
            if (lakeStateManager.getCurrentState() == LakeState.IDLE || lakeStateManager.getCurrentState() == LakeState.ERROR) {
                startConversationalAgent()
            }
        }
    }

    private fun requestLimitIncrease() {
        val userEmail = auth.currentUser?.email
        if (userEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Could not get your email. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val recipient = "ayush0000ayush@gmail.com"
        val subject = "I am facing issue in"
        val body = "Hello,\n\nI am facing issue for my account: $userEmail\n <issue-content>.... \n\nThank you."

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No email application found.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initialize LakeStateManager and set up state change listeners
     */
    private fun initializeLakeStateManager() {
        lakeStateManager = LakeStateManager.getInstance(this)
        stateChangeListener = { newState ->
            updateStatusText(newState)
            updateDeltaVisuals(newState)
            Logger.d("MainActivity", "Lake state changed to: ${newState.name}")
        }
        lakeStateManager.addStateChangeListener(stateChangeListener)
    }

    private fun updateDeltaVisuals(state: LakeState) {
        runOnUiThread {
            val color = DeltaStateColorMapper.getColor(this, state)
            deltaSymbol.setColor(color)

            if (DeltaStateColorMapper.isActiveState(state)) {
                deltaSymbol.startGlow()
            } else {
                deltaSymbol.stopGlow()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        displayDeveloperMessage()
        updateDeltaVisuals(lakeStateManager.getCurrentState())
        updateUI()
        lakeStateManager.startMonitoring()
        
        val wakeWordFilter = IntentFilter(ACTION_WAKE_WORD_FAILED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(wakeWordFailureReceiver, wakeWordFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(wakeWordFailureReceiver, wakeWordFilter)
        }
        
        // Hide tasks left tag since everything is unlimited
        tasksLeftTag.visibility = View.GONE
        
        // Hide pro upgrade banner since all features are free
        val proBanner = findViewById<View>(R.id.pro_upgrade_banner)
        proBanner?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        lakeStateManager.stopMonitoring()
        try {
            unregisterReceiver(wakeWordFailureReceiver)
        } catch (e: IllegalArgumentException) {
            Logger.d("MainActivity", "Receivers were not registered")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::lakeStateManager.isInitialized && ::stateChangeListener.isInitialized) {
            lakeStateManager.removeStateChangeListener(stateChangeListener)
            lakeStateManager.stopMonitoring()
        }
    }

    private fun showDisclaimerDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Disclaimer")
            .setMessage("Lake is an experimental AI assistant and is still in development. It may not always be accurate or perform as expected. It does small task better. Your understanding is appreciated!")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
    }

    private fun showExamplesDialog() {
        val examples = arrayOf(
            "Open YouTube and play music",
            "Send a text message",
            "Set an alarm for 30 minutes",
            "Open camera app",
            "Check weather forecast",
            "Open calculator",
            "Surprise me"
        )
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Example Commands")
            .setItems(examples) { _, which ->
                val selectedExample = examples[which]
                if (selectedExample == "Surprise me"){
                    AgentService.start(this, "play never gonna give you up on youtube")
                }
                AgentService.start(this, selectedExample)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
        
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(this, R.color.black)
        )
    }

    private fun showWakeWordFailureDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_wake_word_failure, null)
        val videoView = dialogView.findViewById<VideoView>(R.id.video_demo)
        val videoContainer = dialogView.findViewById<View>(R.id.video_container_card)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = builder.create()
        lifecycleScope.launch {
            val videoUrl = "https://storage.googleapis.com/lake-app-assets/wake_word_demo.mp4"
            val videoFile: File? = VideoAssetManager.getVideoFile(this@MainActivity, videoUrl)

            if (videoFile != null && videoFile.exists()) {
                videoContainer.visibility = View.VISIBLE
                videoView.setVideoURI(Uri.fromFile(videoFile))
                videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
                }
                alertDialog.setOnShowListener {
                    videoView.start()
                }
            } else {
                Logger.e("MainActivity", "Video file not found, hiding video container.")
                videoContainer.visibility = View.GONE
            }
        }

        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        val allPermissionsGranted = permissionManager.areAllPermissionsGranted()
        if (allPermissionsGranted) {
            tvPermissionStatus.text = "All required permissions are granted."
            tvPermissionStatus.visibility = View.GONE
            managePermissionsButton.visibility = View.GONE
            tvPermissionStatus.setTextColor(Color.parseColor("#4CAF50"))
            permissionsTag.visibility = View.VISIBLE
        } else {
            tvPermissionStatus.text = "Some permissions are missing. Tap below to manage."
            tvPermissionStatus.setTextColor(Color.parseColor("#F44336"))
            permissionsTag.visibility = View.GONE
        }
    }

    private fun isThisAppDefaultAssistant(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            rm?.isRoleHeld(RoleManager.ROLE_ASSISTANT) == true
        } else {
            val flat = Settings.Secure.getString(contentResolver, "voice_interaction_service")
            val currentPkg = flat?.substringBefore('/')
            currentPkg == packageName
        }
    }

    private fun updateDefaultAssistantButtonVisibility() {
        val btn = findViewById<TextView>(R.id.btn_set_default_assistant)
        btn.visibility = if (isThisAppDefaultAssistant()) View.GONE else View.VISIBLE
    }

    private fun displayDeveloperMessage() {
        try {
            val sharedPrefs = getSharedPreferences("developer_message_prefs", Context.MODE_PRIVATE)
            val displayCount = sharedPrefs.getInt("developer_message_count", 0)
            
            if (displayCount >= 1) {
                Logger.d("MainActivity", "Developer message already shown $displayCount times, skipping display")
                return
            }

            val remoteConfig = Firebase.remoteConfig
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        Log.d("MainActivity", "Remote Config params updated: $updated")

                        val message = remoteConfig.getString("developerMessage")

                        if (message.isNotEmpty()) {
                            val dialog = AlertDialog.Builder(this@MainActivity)
                                .setTitle("Message from Developer")
                                .setMessage(message)
                                .setPositiveButton("OK") { dialogInterface, _ ->
                                    dialogInterface.dismiss()
                                }
                                .show()
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                                ContextCompat.getColor(this@MainActivity, R.color.black)
                            )
                            Log.d("MainActivity", "Developer message displayed from Remote Config.")
                        } else {
                            Log.d("MainActivity", "No developer message found in Remote Config.")
                        }
                    } else {
                        Log.e("MainActivity", "Failed to fetch Remote Config.", task.exception)
                    }
                }
        } catch (e: Exception) {
            Logger.e("MainActivity", "Exception in displayDeveloperMessage", e)
        }
    }

    /**
     * Update the status text based on the current LakeState
     */
    fun updateStatusText(state: LakeState) {
        runOnUiThread {
            try {
                val statusText = DeltaStateColorMapper.getStatusText(state)
                statusTextView.text = statusText
                Logger.d("MainActivity", "Status text updated to: $statusText for state: ${state.name}")
            } catch (e: Exception) {
                Logger.e("MainActivity", "Error updating status text", e)
                statusTextView.text = "Ready"
            }
        }
    }

    /**
     * Update the status text with custom text (overrides state-based text)
     */
    fun updateStatusText(customText: String) {
        runOnUiThread {
            try {
                statusTextView.text = customText
                Logger.d("MainActivity", "Status text updated to custom text: $customText")
            } catch (e: Exception) {
                Logger.e("MainActivity", "Error updating status text with custom text", e)
                statusTextView.text = "Ready"
            }
        }
    }
}
