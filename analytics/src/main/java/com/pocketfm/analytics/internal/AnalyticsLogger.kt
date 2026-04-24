package com.pocketfm.analytics.internal

import android.util.Log

internal object AnalyticsLogger {
    private const val TAG = "PFMAnalytics"
    @Volatile
    private var enabled = false

    fun configure(debugEnabled: Boolean) {
        enabled = debugEnabled
    }

    fun d(message: String) {
        if (enabled) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (enabled) {
            Log.e(TAG, message, throwable)
        }
    }
}
