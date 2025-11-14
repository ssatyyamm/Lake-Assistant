# Offline TTS Setup Guide for Lake Assistant

## Overview
Lake Assistant has been converted from online Google Cloud TTS to **fully offline Text-to-Speech** using Piper TTS models. This eliminates the need for internet connectivity and API keys for speech synthesis.

## Changes Made

### 1. New Files Created
- **`app/src/main/java/com/hey/lake/api/PiperTTS.kt`**: Complete offline TTS implementation using Piper models
- Supports multiple offline voices including en_GB-cori-medium

### 2. Modified Files
- **`app/src/main/java/com/hey/lake/api/GoogleTTS.kt`**: Deprecated Google Cloud TTS, now redirects to PiperTTS
- **`app/src/main/java/com/hey/lake/MyApplication.kt`**: Added PiperTTS initialization
- **`app/src/main/java/com/hey/lake/utilities/SpeechCoordinator.kt`**: Updated to use offline TTS
- **`app/build.gradle.kts`**: Dependencies remain compatible (no changes needed)

### 3. How It Works
The app now uses:
1. **Piper TTS** - A fast, local neural text-to-speech system
2. **ONNX Runtime** - For running the neural TTS models
3. **Native binaries** - Compiled Piper executables for Android

All synthesis happens **locally on device** with no internet required.

## Setup Instructions

### Step 1: Download Piper TTS Models

You need to download the following files and place them in your project:

#### Required Model: en_GB-cori-medium (British English Female)
```bash
# Create the models directory
mkdir -p app/src/main/assets/piper_models

# Download the model files (choose one method):

# Method 1: Using wget
cd app/src/main/assets/piper_models
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/cori/medium/en_GB-cori-medium.onnx
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/cori/medium/en_GB-cori-medium.onnx.json

# Method 2: Manual download
# Visit: https://huggingface.co/rhasspy/piper-voices/tree/main/en/en_GB/cori/medium
# Download both files:
# - en_GB-cori-medium.onnx (about 63 MB)
# - en_GB-cori-medium.onnx.json (about 1 KB)
```

#### Optional Additional Voices

For more voice options, download these models to the same directory:

**American English - Amy (Female)**
```bash
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/amy/medium/en_US-amy-medium.onnx
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/amy/medium/en_US-amy-medium.onnx.json
```

**American English - Lessac (Male)**
```bash
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_US/lessac/medium/en_US-lessac-medium.onnx.json
```

**British English - Alba (Female)**
```bash
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alba/medium/en_GB-alba-medium.onnx
wget https://huggingface.co/rhasspy/piper-voices/resolve/main/en/en_GB/alba/medium/en_GB-alba-medium.onnx.json
```

### Step 2: Download Piper Binary for Android

You need the Piper executable compiled for Android ARM64:

```bash
# Create binaries directory
mkdir -p app/src/main/assets/piper_binaries

# Download Piper for Android
# Visit: https://github.com/rhasspy/piper/releases
# Download the appropriate version for Android (arm64-v8a)
# For example: piper_arm64-v8a.tar.gz

# Extract and place the 'piper' executable in:
# app/src/main/assets/piper_binaries/piper

# Make sure the file is named exactly 'piper' (no extension)
```

**Direct download link (example for version 1.2.0):**
```bash
cd app/src/main/assets/piper_binaries
wget https://github.com/rhasspy/piper/releases/download/v1.2.0/piper_arm64-v8a.tar.gz
tar -xzf piper_arm64-v8a.tar.gz
mv piper/piper .
rm -rf piper piper_arm64-v8a.tar.gz
```

### Step 3: Verify Directory Structure

Your assets directory should look like this:

```
app/src/main/assets/
├── piper_models/
│   ├── en_GB-cori-medium.onnx
│   ├── en_GB-cori-medium.onnx.json
│   ├── en_US-amy-medium.onnx (optional)
│   ├── en_US-amy-medium.onnx.json (optional)
│   ├── en_US-lessac-medium.onnx (optional)
│   ├── en_US-lessac-medium.onnx.json (optional)
│   ├── en_GB-alba-medium.onnx (optional)
│   └── en_GB-alba-medium.onnx.json (optional)
└── piper_binaries/
    └── piper (executable file)
```

### Step 4: Build Configuration

The `app/build.gradle.kts` file should already be configured correctly. Verify these settings:

```kotlin
android {
    // ... other settings ...
    
    defaultConfig {
        minSdk = 24  // Minimum Android 7.0
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }
    
    aaptOptions {
        noCompress "onnx", "json"  // Don't compress model files
    }
}
```

### Step 5: Build and Test

1. **Clean and rebuild the project:**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. **Install on device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Test TTS:**
   - Open the app
   - Try speaking with the assistant
   - Voice should work without internet connection
   - Check logcat for any errors: `adb logcat | grep PiperTTS`

## Troubleshooting

### Issue: "Model file not found"
**Solution:** Ensure model files are in `app/src/main/assets/piper_models/` and named correctly.

### Issue: "Piper binary not executable"
**Solution:** The app automatically sets executable permissions. Check that the file exists in `app/src/main/assets/piper_binaries/piper`.

### Issue: "No audio output"
**Solution:** 
- Check device volume settings
- Verify RECORD_AUDIO permission is granted
- Check logcat for audio track errors

### Issue: "Synthesis is slow"
**Solution:**
- Use 'medium' quality models (not 'high')
- Ensure you're testing on real hardware (not emulator)
- ARM64 devices perform better than ARM32

## Model Information

### en_GB-cori-medium
- **Language:** British English
- **Gender:** Female
- **Quality:** Medium (good balance of speed and quality)
- **Sample Rate:** 22050 Hz
- **Model Size:** ~63 MB
- **Speed:** ~0.5x real-time on modern devices

### Performance Expectations
- **Initialization:** ~1-2 seconds on first use
- **Synthesis speed:** 0.3-1.0x real-time (varies by device)
- **Memory usage:** ~100-150 MB during synthesis
- **Storage:** ~65-70 MB per voice model

## Advanced Configuration

### Adding More Languages

To add support for other languages:

1. Browse available models: https://huggingface.co/rhasspy/piper-voices/tree/main
2. Download the .onnx and .onnx.json files
3. Place them in `app/src/main/assets/piper_models/`
4. Add the voice to `OfflineTTSVoice` enum in `PiperTTS.kt`

Example for German:
```kotlin
enum class OfflineTTSVoice {
    // ... existing voices ...
    DE_THORSTEN_MEDIUM("Thorsten (DE)", "de_DE-thorsten-medium", "German male voice")
}
```

### Optimizing Model Size

If storage is a concern:
- Use 'low' quality models (~18 MB each)
- Download only one voice model
- Consider on-demand model downloading from a server

## Benefits of Offline TTS

✅ **No internet required** - Works completely offline  
✅ **No API costs** - Zero recurring costs for TTS  
✅ **Privacy** - All processing happens on device  
✅ **Low latency** - Faster response than cloud API  
✅ **No rate limits** - Unlimited usage  
✅ **Consistent quality** - Not affected by network conditions  

## Migration from Google TTS

The old Google TTS code is still present but deprecated:
- `GoogleTts.synthesize()` now calls `PiperTTS.synthesize()`
- Old voice settings are mapped to offline equivalents
- No code changes needed in most places

To fully remove Google TTS dependencies:
1. Remove `GOOGLE_TTS_API_KEY` from `local.properties`
2. Remove Google TTS related build config fields
3. Remove `GoogleTTS.kt` file (optional, kept for compatibility)

## Support

For issues with:
- **Piper TTS:** https://github.com/rhasspy/piper/issues
- **Voice models:** https://github.com/rhasspy/piper-voices/issues
- **App integration:** Check Lake Assistant documentation

## License

Piper TTS and voice models are licensed under MIT License.
See: https://github.com/rhasspy/piper/blob/master/LICENSE
