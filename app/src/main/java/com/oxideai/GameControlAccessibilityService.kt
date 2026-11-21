package com.oxideai

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent

class GameControlAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: GameControlAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun performTestCameraSwipe(): Boolean {
        val metrics = resources.displayMetrics
        val x = metrics.widthPixels * 0.78f
        val y = metrics.heightPixels * 0.62f
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x - metrics.widthPixels * 0.12f, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 350)
        return dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    fun stopCurrentGesture() {
        // Android accessibility gestures are finite; no persistent loop is started by this prototype.
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }
}
