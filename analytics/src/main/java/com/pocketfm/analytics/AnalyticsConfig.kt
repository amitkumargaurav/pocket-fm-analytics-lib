package com.pocketfm.analytics

data class AnalyticsConfig(
    val apiKey: String,
    val registerUrl: String,
    val ingestUrl: String,
    val flushIntervalMinutes: Long = 15,
    val maxBatchSize: Int = 50,
    val debugLoggingEnabled: Boolean = false,
)
