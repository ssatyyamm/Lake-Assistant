package com.hey.lake

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

/**
 * BaseNavigationActivity - Modified to remove UPGRADE navigation
 * All features are now free, no need for upgrade option
 */
abstract class BaseNavigationActivity : AppCompatActivity() {

    protected abstract fun getContentLayoutId(): Int
    protected abstract fun getCurrentNavItem(): NavItem

    enum class NavItem {
        HOME, TRIGGERS, MOMENTS, SETTINGS
        // Removed UPGRADE - no longer needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableTransitions()
    }
    
    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_base_navigation)
        
        val contentContainer = findViewById<LinearLayout>(R.id.content_container)
        layoutInflater.inflate(layoutResID, contentContainer, true)
        
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val currentItem = getCurrentNavItem()
        
        findViewById<LinearLayout>(R.id.nav_triggers).apply {
            setOnClickListener {
                if (currentItem != NavItem.TRIGGERS) {
                    navigateToActivity(com.hey.lake.triggers.ui.TriggersActivity::class.java, currentItem)
                }
            }
            alpha = if (currentItem == NavItem.TRIGGERS) 1.0f else 0.7f
        }
        
        findViewById<LinearLayout>(R.id.nav_moments).apply {
            setOnClickListener {
                if (currentItem != NavItem.MOMENTS) {
                    navigateToActivity(MomentsActivity::class.java, currentItem)
                }
            }
            alpha = if (currentItem == NavItem.MOMENTS) 1.0f else 0.7f
        }
        
        findViewById<LinearLayout>(R.id.nav_home).apply {
            setOnClickListener {
                if (currentItem != NavItem.HOME) {
                    navigateToActivity(MainActivity::class.java, currentItem)
                }
            }
            alpha = if (currentItem == NavItem.HOME) 1.0f else 0.7f
        }
        
        // Hide the upgrade navigation button since all features are free
        findViewById<LinearLayout>(R.id.nav_upgrade).apply {
            visibility = View.GONE
        }
        
        findViewById<LinearLayout>(R.id.nav_settings).apply {
            setOnClickListener {
                if (currentItem != NavItem.SETTINGS) {
                    navigateToActivity(SettingsActivity::class.java, currentItem)
                }
            }
            alpha = if (currentItem == NavItem.SETTINGS) 1.0f else 0.7f
        }
    }
    
    private fun navigateToActivity(activityClass: Class<*>, currentItem: NavItem) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        disableTransitions()
        if (currentItem != NavItem.HOME) {
            finish()
            disableTransitions()
        }
    }
    
    override fun finish() {
        super.finish()
        disableTransitions()
    }
    
    @Suppress("DEPRECATION")
    private fun disableTransitions() {
        overridePendingTransition(0, 0)
    }
}
