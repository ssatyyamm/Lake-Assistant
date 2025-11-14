# Lake Assistant - Build Guide

## Prerequisites

### Required Software:
1. **Android Studio** (Arctic Fox or newer)
2. **JDK 17** or newer
3. **Android SDK** with the following:
   - Android SDK Build-Tools 34.0.0 or newer
   - Android 14 (API level 34) SDK Platform
4. **Gradle 8.11.1** (included via wrapper)

### API Keys Required:
The app requires the following API keys to function:

1. **Google Gemini API Key** - For AI functionality
2. **Picovoice Access Key** - For wake word detection (optional)
3. **Google Services** - Firebase configuration (google-services.json)

## Setup Steps

### 1. Clone/Open Project
```bash
# Open the project in Android Studio
# File → Open → Select Lake-Assistant directory
```

### 2. Configure API Keys

Create `local.properties` file in the root directory (if not exists):
```properties
# Google Gemini API Key
GEMINI_API_KEY=your_gemini_api_key_here

# Picovoice Access Key (optional)
PICOVOICE_ACCESS_KEY=your_picovoice_key_here

# Android SDK location (usually auto-detected)
sdk.dir=/path/to/Android/Sdk
```

### 3. Add Google Services Configuration

Place your `google-services.json` file in:
```
app/google-services.json
```

If you don't have one, create a Firebase project at:
https://console.firebase.google.com/

## Building the Project

### Option 1: Android Studio (Recommended)
1. Open Android Studio
2. File → Open → Select Lake-Assistant folder
3. Wait for Gradle sync to complete
4. Build → Make Project (Ctrl+F9 / Cmd+F9)
5. Run → Run 'app' (Shift+F10 / Ctrl+R)

### Option 2: Command Line
```bash
# Navigate to project directory
cd Lake-Assistant

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Clean and build debug APK
./gradlew clean assembleDebug

# Build release APK (requires signing config)
./gradlew clean assembleRelease

# Install debug APK on connected device
./gradlew installDebug
```

## Common Build Errors and Solutions

### Error 1: Missing SDK Tools
**Error:** `SDK location not found`

**Solution:**
1. Create `local.properties` in root directory
2. Add: `sdk.dir=/path/to/Android/Sdk`
3. Or let Android Studio auto-generate it

### Error 2: Missing google-services.json
**Error:** `File google-services.json is missing`

**Solution:**
1. Create a Firebase project
2. Download google-services.json
3. Place in `app/` directory

### Error 3: API Key Not Found
**Error:** `GEMINI_API_KEY not found`

**Solution:**
Add to `local.properties`:
```properties
GEMINI_API_KEY=your_actual_api_key
```

### Error 4: Gradle Sync Failed
**Error:** Various Gradle sync errors

**Solution:**
```bash
# Clear Gradle cache
./gradlew clean
rm -rf .gradle
rm -rf build
rm -rf app/build

# Sync again in Android Studio
# File → Sync Project with Gradle Files
```

### Error 5: Kotlin Version Mismatch
**Error:** `Kotlin version mismatch`

**Solution:**
Check `build.gradle.kts` files use consistent Kotlin version:
```kotlin
plugins {
    kotlin("android") version "1.9.0" // Ensure consistent version
}
```

### Error 6: Java Version Mismatch
**Error:** `Unsupported class file major version`

**Solution:**
1. Ensure JDK 17 is installed
2. In Android Studio: File → Project Structure → SDK Location
3. Set JDK to version 17

### Error 7: Missing Wake Word File
**Error:** `Hey-Lake_en_android_v3_0_0.ppn not found`

**Solution:**
1. Ensure wake word file is in `app/src/main/assets/`
2. Or disable wake word in Settings

### Error 8: Unresolved References
**Error:** `Unresolved reference: LakeState` or similar

**Solution:**
1. File → Invalidate Caches → Invalidate and Restart
2. Clean and rebuild project
3. Check that all migration was completed correctly

### Error 9: Resource Not Found
**Error:** `Resource lake_logo not found`

**Solution:**
Ensure Lake logo files exist in:
- `app/src/main/res/drawable/lake_logo.png`
- `app/src/main/res/drawable/lake_logo_v1_512.png`

### Error 10: Duplicate Class Errors
**Error:** `Duplicate class found`

**Solution:**
1. Check no old Panda* classes remain
2. Clean build: `./gradlew clean`
3. Rebuild project

## Build Variants

The project supports multiple build variants:

- **debug** - Development build with debugging enabled
- **release** - Production build (requires signing)

```bash
# List all build variants
./gradlew tasks --all | grep assemble
```

## Signing Configuration

For release builds, add to `local.properties`:
```properties
KEYSTORE_FILE=/path/to/keystore.jks
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test
```bash
./gradlew test --tests "com.hey.lake.utilities.LakeStateManagerTest"
```

## Troubleshooting Tips

### 1. Clean Build
When in doubt, clean everything:
```bash
./gradlew clean
rm -rf .gradle
rm -rf .idea
rm -rf build
rm -rf app/build
```

Then reopen in Android Studio.

### 2. Check Dependencies
Verify internet connection and repository access:
```bash
./gradlew dependencies
```

### 3. Gradle Daemon Issues
If Gradle becomes unresponsive:
```bash
./gradlew --stop
```

### 4. Memory Issues
If build runs out of memory, create/edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

### 5. Check Build Output
For detailed error information:
```bash
./gradlew assembleDebug --stacktrace --info
```

### 6. Verify Migration
Ensure all Panda → Lake migration completed:
```bash
# Should return 0 or only acceptable references
grep -ri "panda" --include="*.kt" --include="*.xml" app/src/
```

## Optimization Tips

### Faster Builds:
1. Enable Gradle daemon (already default)
2. Use parallel execution
3. Enable build cache
4. Use incremental compilation

Add to `gradle.properties`:
```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
kotlin.incremental=true
```

### Reduce Build Time:
- Use Android Studio's "Make Project" instead of full rebuild
- Only build the variant you need
- Use instant run / apply changes when possible

## Deployment

### Debug APK Location:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK Location:
```
app/build/outputs/apk/release/app-release.apk
```

### Install APK:
```bash
# Via Gradle
./gradlew installDebug

# Via ADB
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Next Steps After Successful Build

1. ✅ Test wake word functionality with "Lake" or "Hey Lake"
2. ✅ Verify all UI elements display Lake branding
3. ✅ Test accessibility service functionality
4. ✅ Verify widget displays correctly
5. ✅ Test memory/conversation features
6. ✅ Check notification system
7. ✅ Test trigger system
8. ✅ Verify Pro features

## Support

If you encounter issues not covered here:
1. Check Android Studio Build Output
2. Review stack traces in logs
3. Verify all API keys are correctly configured
4. Ensure all dependencies are available
5. Check the project's GitHub issues page

## Version Information

- **Gradle:** 8.11.1
- **Android Gradle Plugin:** 8.x
- **Kotlin:** 1.9+
- **Compile SDK:** 34 (Android 14)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)

---

**Last Updated:** October 21, 2025  
**Project:** Lake Assistant  
**Status:** Ready for Build
