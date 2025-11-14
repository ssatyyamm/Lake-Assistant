# Lake Assistant - Setup Instructions

## 🚨 CRITICAL: Required Configuration

Lake Assistant **WILL NOT WORK** without proper API key configuration. The error you experienced ("I seem to have gotten my thoughts tangled. Could you repeat that?") is caused by missing API keys.

## ✅ Quick Fix Guide

### Step 1: Get Your Free Gemini API Key

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

### Step 2: Configure the App

1. **Locate the `local.properties` file** in your project root directory:
   ```
   Lake-Assistant/
   ├── app/
   ├── gradle/
   ├── local.properties  ← This file
   └── ...
   ```

2. **Open `local.properties`** and find this line:
   ```properties
   GEMINI_API_KEYS=YOUR_GEMINI_API_KEY_HERE
   ```

3. **Replace `YOUR_GEMINI_API_KEY_HERE`** with your actual API key:
   ```properties
   GEMINI_API_KEYS=AIzaSyAaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQq
   ```

### Step 3: Rebuild and Install

1. **Clean and rebuild** the project:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. **Uninstall the old version** from your device:
   - Settings → Apps → Lake → Uninstall

3. **Install the new build**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   
   OR use Android Studio's Run button

### Step 4: Grant Permissions

When you launch the app for the first time, grant these required permissions:

1. ✅ **Microphone** - For voice commands (CRITICAL)
2. ✅ **Display over other apps** - For floating UI
3. ✅ **Accessibility Service** - For performing tasks
4. ✅ **Notification Access** - For trigger functionality (optional)
5. ✅ **Post Notifications** - For service notifications

## 🔧 Advanced Configuration

### Multiple API Keys (Recommended for Heavy Usage)

To avoid rate limiting, you can add multiple Gemini API keys:

```properties
GEMINI_API_KEYS=key1,key2,key3
```

The app will automatically rotate between keys.

### Optional API Keys

These are optional but enable additional features:

```properties
# Tavily Search API - For web search functionality
# Get from: https://tavily.com/
TAVILY_API=your_tavily_key_here

# Mem0 API - For enhanced memory management
# Get from: https://mem0.ai/
MEM0_API=your_mem0_key_here

# Picovoice Access Key - For "Hey Lake" wake word detection
# Get from: https://picovoice.ai/
PICOVOICE_ACCESS_KEY=your_picovoice_key_here
```

## 🐛 Troubleshooting

### Error: "I seem to have gotten my thoughts tangled"

**Cause**: Gemini API key is not configured or invalid

**Solution**:
1. Verify your API key in `local.properties`
2. Make sure it's a valid Gemini API key (starts with `AIzaSy`)
3. Check you didn't leave `YOUR_GEMINI_API_KEY_HERE` as the value
4. Rebuild the app after making changes

### Error: "Some permissions are missing"

**Cause**: Required Android permissions not granted

**Solution**:
1. Tap "Manage Permissions" button in the app
2. Grant all required permissions
3. Return to the app - the error should disappear

### App Crashes on Launch

**Cause**: Missing Google Services configuration

**Solution**:
1. Ensure `google-services.json` is in the `app/` directory
2. If missing, download it from Firebase Console:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project
   - Project Settings → General → Download `google-services.json`
   - Place it in `Lake-Assistant/app/` directory

### Voice Input Not Working

**Cause**: Microphone permission not granted or service error

**Solution**:
1. Check microphone permission: Settings → Apps → Lake → Permissions
2. Ensure "Microphone" is allowed
3. Check logs for errors: `adb logcat -s ConvAgent`

## 📱 Testing the Fix

After configuration, test these scenarios:

### Test 1: Text Input
1. Launch Lake app
2. Tap the delta (△) symbol
3. Tap "+ Ask Lake" input field
4. Type "hi" and press enter
5. ✅ Should respond successfully (not "thoughts tangled" error)

### Test 2: Voice Input
1. Launch Lake app
2. Tap the delta (△) symbol
3. Wait for "Listening..." state
4. Say "hello"
5. ✅ Should respond successfully

### Test 3: Task Execution
1. Launch Lake app
2. Tap the delta (△) symbol
3. Say or type "open YouTube"
4. ✅ Should confirm and open YouTube app

## 🔐 Security Notes

- **NEVER commit `local.properties`** to version control (already in `.gitignore`)
- Keep your API keys private and secure
- Rotate keys regularly if sharing builds with others
- Monitor your API usage at [Google AI Studio](https://makersuite.google.com/)

## 📊 API Key Limits

**Free Tier Gemini API:**
- **60 requests per minute**
- **1,500 requests per day**
- **1 million tokens per month**

For heavy usage, consider:
1. Adding multiple API keys (load balancing)
2. Upgrading to Gemini Advanced (paid tier)
3. Using the proxy configuration (enterprise setups)

## 🆘 Still Having Issues?

If you continue to experience problems after following this guide:

1. **Check the logs**:
   ```bash
   adb logcat -s ConvAgent:* GeminiApi:* ApiKeyManager:*
   ```

2. **Verify API key validity**:
   - Test your key manually at [AI Studio](https://aistudio.google.com/)

3. **Email support**:
   - From the app: Tap "Email Developer"
   - Include: Device model, Android version, error logs

## ✨ What's Fixed in This Version

✅ **Fixed**: Missing API key now shows clear error message instead of "thoughts tangled"  
✅ **Fixed**: Better API key validation and error handling  
✅ **Added**: Comprehensive setup documentation  
✅ **Added**: `local.properties` template with instructions  
✅ **Improved**: Error messages now actionable and user-friendly  

---

**Project**: Lake Assistant  
**Version**: 1.0.13+  
**Last Updated**: 2025-11-11  
**Developer**: [Your Name/Team]  
