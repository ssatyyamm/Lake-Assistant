package com.hey.lake.utilities

import android.content.Context
import androidx.core.content.ContextCompat
import com.hey.lake.R

/**
 * Utility class for mapping LakeState values to their corresponding colors
 * and providing state-related information for the delta symbol.
 */
object DeltaStateColorMapper {

    /**
     * Data class representing the visual state of the delta symbol
     */
    data class DeltaVisualState(
        val state: LakeState,
        val color: Int,
        val statusText: String,
        val colorHex: String
    )

    /**
     * Get the color resource ID for a given LakeState
     */
    fun getColorResourceId(state: LakeState): Int {
        return when (state) {
            LakeState.IDLE -> R.color.delta_idle
            LakeState.LISTENING -> R.color.delta_listening
            LakeState.PROCESSING -> R.color.delta_processing
            LakeState.SPEAKING -> R.color.delta_speaking
            LakeState.ERROR -> R.color.delta_error
        }
    }

    /**
     * Get the resolved color value for a given LakeState
     */
    fun getColor(context: Context, state: LakeState): Int {
        val colorResId = getColorResourceId(state)
        return ContextCompat.getColor(context, colorResId)
    }

    /**
     * Get the status text for a given LakeState
     */
    fun getStatusText(state: LakeState): String {
        return when (state) {
            LakeState.IDLE -> "Ready, tap delta to wake me up!"
            LakeState.LISTENING -> "Listening..."
            LakeState.PROCESSING -> "Processing..."
            LakeState.SPEAKING -> "Speaking..."
            LakeState.ERROR -> "Error"
        }
    }

    /**
     * Get the hex color string for a given LakeState (for debugging/logging)
     */
    fun getColorHex(context: Context, state: LakeState): String {
        val color = getColor(context, state)
        return String.format("#%08X", color)
    }

    /**
     * Get complete visual state information for a given LakeState
     */
    fun getDeltaVisualState(context: Context, state: LakeState): DeltaVisualState {
        return DeltaVisualState(
            state = state,
            color = getColor(context, state),
            statusText = getStatusText(state),
            colorHex = getColorHex(context, state)
        )
    }

    /**
     * Get all available states with their visual information
     */
    fun getAllStates(context: Context): List<DeltaVisualState> {
        return LakeState.values().map { state ->
            getDeltaVisualState(context, state)
        }
    }

    /**
     * Check if a state represents an active operation (not idle or error)
     */
    fun isActiveState(state: LakeState): Boolean {
        return when (state) {
            LakeState.LISTENING, LakeState.PROCESSING, LakeState.SPEAKING -> true
            LakeState.IDLE, LakeState.ERROR -> false
        }
    }

    /**
     * Check if a state represents an error condition
     */
    fun isErrorState(state: LakeState): Boolean {
        return state == LakeState.ERROR
    }

    /**
     * Get the priority of a state for determining which state to display
     * when multiple conditions might be true. Higher numbers = higher priority.
     */
    fun getStatePriority(state: LakeState): Int {
        return when (state) {
            LakeState.ERROR -> 5      // Highest priority
            LakeState.SPEAKING -> 4   // High priority
            LakeState.LISTENING -> 3  // Medium-high priority
            LakeState.PROCESSING -> 2 // Medium priority
            LakeState.IDLE -> 1       // Lowest priority
        }
    }
}