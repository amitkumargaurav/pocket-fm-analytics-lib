package com.pocketfm.analytics.internal

import android.content.Context
import com.pocketfm.analytics.AnalyticsConfig

internal class StoredConfigStore(context: Context) {
    private val prefs = context.getSharedPreferences("pfm_analytics_config", Context.MODE_PRIVATE)

    fun save(config: AnalyticsConfig) {
        prefs.edit()
            .putString("api_key", config.apiKey)
            .putString("register_url", config.registerUrl)
            .putString("ingest_url", config.ingestUrl)
            .putLong("flush_interval", config.flushIntervalMinutes)
            .putInt("batch_size", config.maxBatchSize)
            .putBoolean("debug", config.debugLoggingEnabled)
            .apply()
    }

    fun read(): AnalyticsConfig? {
        val apiKey = prefs.getString("api_key", null) ?: return null
        val registerUrl = prefs.getString("register_url", null) ?: return null
        val ingestUrl = prefs.getString("ingest_url", null) ?: return null
        return AnalyticsConfig(
            apiKey = apiKey,
            registerUrl = registerUrl,
            ingestUrl = ingestUrl,
            flushIntervalMinutes = prefs.getLong("flush_interval", 15L),
            maxBatchSize = prefs.getInt("batch_size", 50),
            debugLoggingEnabled = prefs.getBoolean("debug", false),
        )
    }
}
