package com.hey.lake

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ProPurchaseActivity - DEPRECATED
 * This activity is no longer used as all features are now free and unlimited.
 * Kept for compatibility but immediately closes with a message.
 */
class ProPurchaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show message that all features are free
        Toast.makeText(
            this, 
            "All features are now free and unlimited! Enjoy Lake Assistant.", 
            Toast.LENGTH_LONG
        ).show()
        
        // Close this activity immediately
        finish()
    }
}
