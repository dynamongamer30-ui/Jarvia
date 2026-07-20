package com.example.actions

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class JarvisAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return
        
        // Typically read node tree to perform clicks inside 3rd party apps.
        // For example, finding WhatsApp text box, pasting, and clicking send.
        
        rootNode.recycle()
    }

    override fun onInterrupt() {
        Log.d("JarvisAccessibility", "Service Interrupted")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("JarvisAccessibility", "Service Connected")
    }
}
