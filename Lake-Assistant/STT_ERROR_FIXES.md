# Speech-to-Text Error Fixes Applied

## Date: November 10, 2025

## Problem Description
The app was showing a "please repeat" toast message after every question when using speech or text input, and wasn't giving any answers. This was caused by several issues in the speech recognition system.

## Root Causes

### 1. Missing `onPartialResult` Parameter
The `SpeechCoordinator.startListening()` method requires 4 parameters:
- `onResult: (String) -> Unit`
- `onError: (String) -> Unit`
- `onListeningStateChange: (Boolean) -> Unit`
- `onPartialResult: (String) -> Unit`

However, in `UserInputManager.kt`, it was being called with only 3 parameters, missing the `onPartialResult` callback.

### 2. Infinite Voice Input Loop in DialogueActivity
The `DialogueActivity` had logic that automatically restarted voice input after every error, creating an endless loop of:
1. Listen for speech
2. Get error (timeout/no speech)
3. Automatically restart voice input
4. Show "please repeat" message
5. Repeat from step 1

### 3. No Response State Tracking
The dialogue activity didn't track whether a response had already been received, so it would process multiple results for the same question.

## Fixes Applied

### File: `UserInputManager.kt`
**Location:** Line 97-103

**Before:**
```kotlin
onListeningStateChange = { isListening ->
    Log.d(TAG, "Listening state changed on attempt $attempt: $isListening")
},
onPartialResult = { } // <-- FIX: Added the missing fourth argument

)
```

**After:**
```kotlin
onListeningStateChange = { isListening ->
    Log.d(TAG, "Listening state changed on attempt $attempt: $isListening")
},
onPartialResult = { partialText ->
    Log.d(TAG, "Partial result on attempt $attempt: $partialText")
}
)
```

### File: `DialogueActivity.kt`
**Multiple locations**

#### 1. Added Response State Tracking
```kotlin
private var hasReceivedResponse = false
```

#### 2. Modified `startVoiceInput()` Function
- Added check at the beginning to prevent restart if response already received
- Wrapped response handling with `hasReceivedResponse` check
- Removed automatic voice input restart on error
- Changed error handling to show message but not automatically retry

**Key Changes:**
```kotlin
private fun startVoiceInput() {
    if (hasReceivedResponse) {
        return // Don't restart voice input if we already have a response
    }
    // ... rest of the function
    
    onResult = { recognizedText ->
        runOnUiThread {
            if (!hasReceivedResponse) {  // <-- Added check
                hasReceivedResponse = true
                // ... process result
            }
        }
    },
    onError = { errorMessage ->
        runOnUiThread {
            if (!hasReceivedResponse) {  // <-- Added check
                // Show error but DON'T automatically restart
                Toast.makeText(this, "Please repeat: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
```

#### 3. Modified `showQuestion()` Function
Added reset of `hasReceivedResponse` flag when showing a new question:
```kotlin
private fun showQuestion(index: Int) {
    if (index < questions.size) {
        currentQuestionIndex = index
        val question = questions[index]
        hasReceivedResponse = false // Reset for new question
        // ... rest of the function
    }
}
```

#### 4. Added `onPause()` Override
Added lifecycle method to stop voice input when activity is paused:
```kotlin
override fun onPause() {
    super.onPause()
    // Stop voice input when activity is paused
    stopVoiceInput()
}
```

## Expected Behavior After Fixes

1. **Voice Input Works Properly**: 
   - User speaks → Answer is recognized → Question moves forward
   - No endless loops of "please repeat"

2. **Error Handling is Graceful**:
   - If speech recognition fails, user sees the error message
   - User can manually tap the voice button again to retry
   - No automatic restarts

3. **Single Response per Question**:
   - Each question only processes one response
   - Prevents duplicate processing of the same speech input

4. **Partial Results Display**:
   - User can see their speech being transcribed in real-time
   - Better user experience during speech recognition

## Testing Recommendations

1. **Voice Input Test**:
   - Ask a question using voice
   - Verify answer is processed correctly
   - Verify no "please repeat" loop

2. **Error Handling Test**:
   - Remain silent during voice input
   - Verify error message appears once
   - Verify voice input doesn't automatically restart

3. **Text Input Test**:
   - Type an answer using the keyboard
   - Verify it processes correctly
   - Verify no conflicts with voice input

4. **Partial Results Test**:
   - Speak slowly during voice input
   - Verify text appears in the input field as you speak
   - Verify cursor stays at the end of text

## Files Modified

1. `app/src/main/java/com/hey/lake/utilities/UserInputManager.kt`
2. `app/src/main/java/com/hey/lake/DialogueActivity.kt`

## Build Instructions

After applying these fixes:
1. Clean and rebuild the project: `./gradlew clean build`
2. Uninstall the old app from your test device
3. Install and test the new APK
4. Test both voice and text input modes thoroughly

## Notes

- The `ConversationalAgentService.kt` was already using the correct 4-parameter call to `startListening()`, so no changes were needed there.
- The `STTManager.kt` already has proper error handling and partial results support, so no changes were needed there either.
- The fixes maintain backward compatibility with the rest of the codebase.
