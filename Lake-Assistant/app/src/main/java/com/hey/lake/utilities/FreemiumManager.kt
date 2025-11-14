package com.hey.lake.utilities

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * FreemiumManager - Modified to make all features free and unlimited
 * All subscription checks now return true/unlimited values
 */
class FreemiumManager {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    companion object {
        const val DAILY_TASK_LIMIT = Int.MAX_VALUE // Unlimited tasks
    }

    suspend fun getDeveloperMessage(): String {
        return try {
            val document = db.collection("settings").document("freemium").get().await()
            document.getString("developerMessage") ?: ""
        } catch (e: Exception) {
            Log.e("FreemiumManager", "Error fetching developer message from Firestore.", e)
            ""
        }
    }

    /**
     * Always returns true - all users have unlimited access
     */
    suspend fun isUserSubscribed(): Boolean {
        return true // Everyone is considered subscribed (premium)
    }

    suspend fun provisionUserIfNeeded() {
        val currentUser = auth.currentUser ?: return
        val userDocRef = db.collection("users").document(currentUser.uid)

        try {
            val document = userDocRef.get().await()
            if (!document.exists()) {
                Logger.d("FreemiumManager", "Provisioning new user: ${currentUser.uid}")
                val newUser = hashMapOf(
                    "email" to currentUser.email,
                    "plan" to "pro", // All users get pro plan
                    "createdAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(newUser).await()
            } else {
                // Update existing users to pro plan
                userDocRef.update("plan", "pro").await()
            }
        } catch (e: Exception) {
            Logger.e("FreemiumManager", "Error provisioning user", e)
        }
    }

    /**
     * Always returns unlimited tasks
     */
    suspend fun getTasksRemaining(): Long {
        return Long.MAX_VALUE // Unlimited tasks
    }

    /**
     * Always returns true - users can always perform tasks
     */
    suspend fun canPerformTask(): Boolean {
        return true // Always allow tasks
    }

    /**
     * No-op method - task count is unlimited, no need to decrement
     */
    suspend fun decrementTaskCount() {
        // Do nothing - unlimited tasks
        Logger.d("FreemiumManager", "Task performed (unlimited mode)")
    }
}
