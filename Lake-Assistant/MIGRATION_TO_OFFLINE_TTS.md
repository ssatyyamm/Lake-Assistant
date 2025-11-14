# Migration to Offline TTS - Lake Assistant

## Date: October 21, 2025
## Version: 1.0.13 (Offline Edition)

---

## 🎯 Summary of Changes

This document outlines all changes made to migrate Lake Assistant from Google Cloud TTS to fully offline PiperTTS, and to rename all "Blurr" references to "Lake".

---

## 📋 Changes Made

### 1. **TTS System Migration** ✅

#### Removed Google TTS Dependencies
- **GoogleTTS.kt**: Completely deprecated and replaced with stub file
- **Build Configuration**: Commented out GOOGLE_TTS_API_KEY in `app/build.gradle.kts`
- **API Key**: No longer required in `local.properties`

#### Implemented Offline TTS (PiperTTS)
- **PiperTTS.kt**: Already implemented with full offline support
- **TTSManager.kt**: Updated to use `PiperTTS` and `OfflineTTSVoice` exclusively
  - Changed all `GoogleTts.synthesize()` calls to `PiperTTS.synthesize()`
  - Updated voice type from `TTSVoice` to `OfflineTTSVoice`
  - Added offline-specific cache functions
  - Removed all Google TTS fallback code

#### Updated Voice Management
- **VoicePreferenceManager.kt**: Complete rewrite to use `OfflineTTSVoice`
  - Default voice: `EN_GB_CORI_MEDIUM` (British English female)
  - Updated preference key to `selected_voice_offline`
- **SettingsActivity.kt**: Updated to display offline voices
  - Changed imports to use `OfflineTTSVoice` and `PiperTTS`
  - Voice picker now shows: Cori, Amy, Lessac, Alba

#### Initialization
- **MyApplication.kt**: Already initializes PiperTTS on app startup
- Offline TTS ready to use without internet connection

---

### 2. **Branding Migration: Blurr → Lake** ✅

#### Renamed All References
- **Package Names**: Updated from `com.blurr.app` to `com.hey.lake`
- **Class Names**: `BlurrTheme` → `LakeTheme`
- **Themes**: `Theme.Blurr` → `Theme.Lake` (XML)
- **Constants**: 
  - `BlurrSettings` → `LakeSettings`
  - `BlurrVoice` → `LakeVoice`
  - `blurr_memory_database` → `lake_memory_database`
- **UI Text**: 
  - "Blurr Wake Word" → "Lake Wake Word"
  - "Blurr Trigger Service" → "Lake Trigger Service"
- **Repository Reference**: Updated to `ayush0chaudhary/lake`
- **Asset URLs**: Updated storage URLs from `blurr-app-assets` to match project

#### Files Modified for Branding
- `ExampleInstrumentedTest.kt`: Package name assertion
- `AndroidManifest.xml`: All theme references
- `Finger.kt`: ChatActivity className
- `ConversationalAgentService.kt`: Repository reference
- `AppDatabase.kt`: Database name
- `MainActivity.kt`: Asset URLs
- `EnhancedWakeWordService.kt`: Notification title
- `WakeWorkService.kt`: Notification title
- `SettingsActivity.kt`: SharedPreferences name
- `TriggerMonitoringService.kt`: Notification title
- `Theme.kt`: Theme function name
- `Logger.kt`: Default tag
- `VoicePreferenceManager.kt`: SharedPreferences name
- `themes.xml`: Theme style name

---

## 📦 Project Structure

```
Lake-Assistant/
├── app/
│   ├── build.gradle.kts                    [MODIFIED - Commented out Google TTS API key]
│   └── src/main/
│       ├── java/com/hey/lake/
│       │   ├── api/
│       │   │   ├── GoogleTTS.kt            [DEPRECATED - Stub file only]
│       │   │   └── PiperTTS.kt             [ACTIVE - Offline TTS implementation]
│       │   ├── utilities/
│       │   │   ├── TTSManager.kt           [MODIFIED - Uses PiperTTS exclusively]
│       │   │   └── VoicePreferenceManager.kt [REWRITTEN - Offline voices only]
│       │   ├── SettingsActivity.kt         [MODIFIED - Offline voice selection]
│       │   └── MyApplication.kt            [VERIFIED - PiperTTS initialization]
│       ├── res/values/
│       │   ├── strings.xml                 [UPDATED - Lake branding]
│       │   └── themes.xml                  [UPDATED - Theme.Lake]
│       └── AndroidManifest.xml             [UPDATED - Theme.Lake references]
├── CHANGES_SUMMARY.md                      [EXISTING - Free version documentation]
├── OFFLINE_TTS_SETUP.md                    [EXISTING - Piper TTS setup guide]
└── MIGRATION_TO_OFFLINE_TTS.md             [NEW - This file]
```

---

## 🚀 Building the Project

### Prerequisites
1. Android Studio (latest version)
2. JDK 11 or higher
3. Android SDK API 35

### Build Steps

```bash
# 1. Open in Android Studio
cd Lake-Assistant
# Open the folder in Android Studio

# 2. Sync Gradle dependencies
# Android Studio will auto-sync, or manually:
# File → Sync Project with Gradle Files

# 3. Clean and build
./gradlew clean assembleDebug

# 4. Install on device
./gradlew installDebug
# Or use Android Studio's Run button
```

### Important Notes

⚠️ **Piper TTS Models Required**
- The app requires Piper TTS model files to function properly
- Models should be placed in: `app/src/main/assets/piper_models/`
- See `OFFLINE_TTS_SETUP.md` for detailed setup instructions
- Default model: `en_GB-cori-medium.onnx` (British English female)

📝 **Configuration File**
- Create/update `local.properties` file
- GOOGLE_TTS_API_KEY is no longer needed
- Other API keys still required (Gemini, Firebase, etc.)

---

## ✅ Testing Checklist

After building, verify:

- [ ] App launches without TTS errors
- [ ] Voice output works offline (airplane mode test)
- [ ] Voice selection shows offline voices (Cori, Amy, Lessac, Alba)
- [ ] No "Google TTS" references in logs
- [ ] App name displays as "Lake" everywhere
- [ ] Theme is "Theme.Lake"
- [ ] No "Blurr" references visible in UI
- [ ] Wake word service shows "Lake Wake Word"
- [ ] Settings use "LakeSettings" preferences
- [ ] All features work without internet (except AI queries)

---

## 🔧 Troubleshooting

### Issue: "PiperTTS model not found"
**Solution**: 
- Download Piper TTS models from HuggingFace
- Place in `app/src/main/assets/piper_models/`
- See `OFFLINE_TTS_SETUP.md` for download links

### Issue: "No audio output"
**Solution**:
- Check device volume
- Verify audio permissions granted
- Check logcat: `adb logcat | grep PiperTTS`
- Ensure model files are properly copied to assets

### Issue: Build errors about TTSVoice
**Solution**:
- Clean project: `./gradlew clean`
- Invalidate caches: Android Studio → File → Invalidate Caches / Restart
- Ensure all imports use `OfflineTTSVoice` not `TTSVoice`

### Issue: "Google TTS API key missing"
**Solution**:
- This is expected and safe to ignore
- Build config provides empty string
- All TTS now uses offline models

---

## 📊 Code Changes Summary

### Files Modified: 15
### Files Deprecated: 1 (GoogleTTS.kt)
### New Features: Fully offline TTS
### Removed Dependencies: Google Cloud TTS API
### Branding Updates: 20+ references changed

---

## 🎯 Key Benefits

### For Users
✅ **Complete Offline Operation** - TTS works without internet
✅ **No API Costs** - Zero recurring costs for speech synthesis
✅ **Privacy** - All voice processing happens on device
✅ **Faster Response** - No network latency
✅ **Unlimited Usage** - No API rate limits

### For Developers
✅ **Simpler Architecture** - No cloud API dependencies
✅ **Easier Testing** - Works in airplane mode
✅ **Cleaner Code** - Single TTS implementation
✅ **Consistent Branding** - All "Lake" references
✅ **Better Maintenance** - Fewer external dependencies

---

## 📝 Next Steps

1. **Download Piper Models** (if not already done)
   ```bash
   cd app/src/main/assets
   mkdir -p piper_models
   # Download from: https://huggingface.co/rhasspy/piper-voices
   ```

2. **Test Build**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install and Test**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Verify Offline Mode**
   - Enable airplane mode
   - Test voice commands
   - Check voice output quality

---

## 🔗 Related Documentation

- `OFFLINE_TTS_SETUP.md` - Detailed Piper TTS setup guide
- `CHANGES_SUMMARY.md` - Free version changes documentation
- `BUILD_GUIDE.md` - General build instructions
- HuggingFace Piper Voices: https://huggingface.co/rhasspy/piper-voices

---

## ✨ Status: Ready to Build!

All changes have been applied. The project is now:
- ✅ Fully offline TTS capable
- ✅ Branded as "Lake" throughout
- ✅ Free of Google TTS dependencies
- ✅ Ready for testing and deployment

---

## 📅 Change Log

**October 21, 2025**
- Migrated from Google Cloud TTS to PiperTTS
- Renamed all "Blurr" references to "Lake"
- Updated voice management for offline voices
- Deprecated GoogleTTS.kt
- Updated all related documentation

---

**Need Help?** Check the troubleshooting section or review `OFFLINE_TTS_SETUP.md` for detailed guidance.
