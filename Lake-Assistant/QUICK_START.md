# 🚀 Lake Assistant - Quick Start Guide

## Welcome to Lake Assistant!
Your personal AI assistant with offline voice capabilities and intelligent conversation.

---

## ⚡ Quick Setup (5 Minutes)

### Step 1: Get Your API Keys (Free Tiers Available)

#### 1️⃣ **Gemini AI Key** (Required) ✅ ALREADY CONFIGURED
- Your key: `AIzaSyAM7wimPRMshzwvegkeDgBAKFyPoZFZd1U`
- Get yours at: https://aistudio.google.com/apikey
- Free tier: 60 requests/minute

#### 2️⃣ **Tavily Search Key** (Required) ✅ ALREADY CONFIGURED
- Your key: `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`
- Get yours at: https://tavily.com/
- Free tier: 1000 searches/month

#### 3️⃣ **Mem0 API Key** (Required) ✅ ALREADY CONFIGURED
- Your key: `m0-cqfrvBF2DW9QdCFmwjq749bzS4L5gwOSyishK08t`
- Get yours at: https://mem0.ai/
- Free tier: 10,000 memories

#### 4️⃣ **Picovoice Key** (Optional - for "Hey Lake!" wake word)
- Get yours at: https://console.picovoice.ai/
- Free tier: 3 devices, 1 wake word
- Configure in app Settings screen

---

## 📱 Build & Install

### Option A: Android Studio (Recommended)
```bash
1. Open project in Android Studio
2. Wait for Gradle sync to complete
3. Click "Run" (Shift+F10) or the green play button
4. Select your device/emulator
5. App installs automatically
```

### Option B: Command Line
```bash
# Debug build
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk

# Release build (requires signing)
./gradlew assembleRelease
```

---

## 🎯 Key Features

### ✅ What Works Out of the Box:

1. **Offline Text-to-Speech**
   - Voice: Cori (British English, Female)
   - No internet required for voice
   - Natural sounding speech

2. **AI Conversation**
   - Powered by Gemini 2.5 Flash
   - Context-aware responses
   - Long-term memory via Mem0

3. **Web Search Integration**
   - Real-time information via Tavily
   - Search results integrated into conversations

4. **Wake Word Detection** (Optional)
   - Say "Hey Lake!" to activate
   - Requires Picovoice key
   - Background listening capability

---

## ⚙️ First-Time Setup in App

### 1. Launch App
- Grant necessary permissions when prompted
- Allow microphone access (for voice input)
- Allow overlay permission (for captions)
- Allow notification access (for background wake word)

### 2. Configure Settings
**Navigation:** Main Screen → Settings Icon (⚙️)

#### Voice Settings:
- **Voice Picker**: Select "Cori (GB) (Downloaded)"
- Voice sample plays automatically
- Test different phrases

#### Wake Word (Optional):
- Paste your Picovoice key
- Click "Enable Wake Word"
- Grant required permissions
- Test by saying "Hey Lake!"

#### Profile (Optional):
- Enter your name
- Enter your email
- Helps AI personalize responses

---

## 🗣️ Using Lake Assistant

### Voice Conversation:
1. **Tap microphone button** on main screen
2. **Speak your question** or command
3. **Lake listens** and processes
4. **Response plays** via offline TTS
5. **Captions appear** on screen (if enabled)

### Text Conversation:
1. **Tap chat bubble icon**
2. **Type your message**
3. **Send** and wait for response
4. **Voice playback** automatic

### Wake Word Activation:
1. **Say "Hey Lake!"** when app is in background
2. **App activates** and starts listening
3. **Speak your request**
4. **Response delivered** with TTS

---

## 🎨 UI Overview

### Main Screen Elements:
```
┌─────────────────────────────────┐
│     🌊 Lake Assistant 🌊         │
├─────────────────────────────────┤
│                                  │
│    [Large Microphone Button]    │ ← Tap to speak
│                                  │
│    [Audio Wave Visualization]   │ ← Shows when listening
│                                  │
│    ┌─────┐  ┌─────┐  ┌─────┐   │
│    │ 💬  │  │ 🔍  │  │ ⚙️  │   │ ← Bottom nav
│    └─────┘  └─────┘  └─────┘   │
└─────────────────────────────────┘
```

### Settings Screen:
```
┌─────────────────────────────────┐
│         ⚙️ Settings              │
├─────────────────────────────────┤
│                                  │
│  📢 Voice & Speech               │
│  ┌───────────────────────────┐  │
│  │   Select Voice:           │  │
│  │   ┌─────────────────┐     │  │
│  │   │ Cori (GB)       │ ←   │  │ Number Picker
│  │   │ (Downloaded) ✓  │     │  │
│  │   └─────────────────┘     │  │
│  └───────────────────────────┘  │
│                                  │
│  🔔 Enable 'Hey Lake!'           │
│  ┌───────────────────────────┐  │
│  │ Picovoice Key: [_______] │  │ ← Paste key here
│  │ [Enable Wake Word]        │  │ ← Activate
│  └───────────────────────────┘  │
│                                  │
│  👤 Profile                      │
│  Name:  [____________]           │
│  Email: [____________]           │
│                                  │
│  [Lake not working? Help]        │
│                                  │
└─────────────────────────────────┘
```

---

## 🔧 Troubleshooting

### ❌ "API key invalid" error
**Solution:**
1. Check `local.properties` file exists
2. Verify API key format (no extra spaces)
3. Rebuild project: `./gradlew clean build`
4. Check API key quotas online

### ❌ Voice doesn't play sample
**Solution:**
1. Check logcat for errors: `adb logcat | grep PiperTTS`
2. Verify model files exist: `app/src/main/assets/piper_models/`
3. Ensure permissions granted
4. Try restarting app

### ❌ Wake word not working
**Solution:**
1. Verify Picovoice key is valid
2. Check all permissions granted:
   - Microphone
   - Notification
   - Battery optimization disabled
3. Ensure service is running (check notification)
4. Test with app in foreground first

### ❌ "Lake not responding"
**Solution:**
1. Check internet connection (required for AI)
2. Verify Gemini API quota not exceeded
3. Clear app cache: Settings → Apps → Lake → Clear Cache
4. Check battery optimization settings

---

## 📊 Performance Tips

### 🚀 Speed Up Responses:
- Enable TTS caching (on by default)
- Use shorter questions/prompts
- Keep memory size reasonable
- Use offline TTS (already enabled)

### 🔋 Save Battery:
- Disable wake word when not needed
- Reduce screen brightness
- Close background apps
- Use power saving mode

### 📶 Reduce Data Usage:
- TTS is offline (no data used)
- Limit web searches
- Use cached responses when possible
- Download conversations for offline reading

---

## 📚 Advanced Features

### Custom Prompts:
Edit system prompts in:
- `app/src/main/java/com/hey/lake/agent/v2/prompts/SystemPrompt.kt`

### Add More Voices:
1. Download Piper models: https://github.com/rhasspy/piper/releases
2. Add to: `app/src/main/assets/piper_models/`
3. Update enum: `api/PiperTTS.kt`

### Custom Wake Word:
1. Upgrade Picovoice to paid tier
2. Train custom wake word in console
3. Download `.ppn` file
4. Add to app assets

### Memory Management:
Configure in `utilities/MemoryManager.kt`:
- Max memory items
- Memory retention period
- Memory importance scoring

---

## 🐛 Debug Mode

### Enable Debug Logging:
```kotlin
// In MyApplication.kt
BuildConfig.ENABLE_LOGGING = true
```

### View Logs:
```bash
# All logs
adb logcat | grep -E "Lake|Gemini|Piper|TTS"

# TTS only
adb logcat | grep PiperTTS

# API calls only
adb logcat | grep GeminiApi
```

### Check Cache:
```bash
# TTS cache
adb shell run-as com.hey.lake ls -la /data/data/com.hey.lake/cache/tts_cache/

# Voice samples
adb shell run-as com.hey.lake ls -la /data/data/com.hey.lake/cache/voice_samples/
```

---

## 📖 Documentation

### Code Documentation:
- **API Reference**: See inline comments in source files
- **Architecture**: See `docs/ARCHITECTURE.md`
- **Contributing**: See `docs/CONTRIBUTING.md`

### External Docs:
- **Gemini API**: https://ai.google.dev/gemini-api/docs
- **Piper TTS**: https://github.com/rhasspy/piper
- **Tavily Search**: https://docs.tavily.com/
- **Mem0 Memory**: https://docs.mem0.ai/

---

## ✅ Checklist Before Using

- [ ] API keys configured in `local.properties`
- [ ] Project builds without errors
- [ ] App installs on device/emulator
- [ ] Permissions granted (mic, overlay, notifications)
- [ ] Voice sample plays in Settings
- [ ] Can send text message and get response
- [ ] Can speak and get voice response
- [ ] Wake word configured (optional)

---

## 🎉 You're Ready!

Lake Assistant is now ready to use. Try these commands:

- "What's the weather today?"
- "Tell me a joke"
- "Search for the latest news on AI"
- "Remind me about our conversation"
- "Hey Lake! What can you do?"

Enjoy your personal AI assistant! 🚀

---

**Need Help?**
- Check `FIXES_2025.md` for detailed technical info
- Review `README.md` for project overview
- Check logs for error messages
- Open an issue on project repository

**Version**: 1.0.13  
**Last Updated**: October 27, 2025
