package com.pocketfm.analytics

enum class AnalyticsEventType {
    IMPRESSION,
    SCREEN_TIME,
    CLICK,
}

data class AnalyticsEvent(
    val eventId: String,
    val type: AnalyticsEventType,
    val clientTimestampMs: Long,
    val sessionId: String,
    val anonymousId: String,
    val userId: String?,
    val screenName: String?,
    val metadata: Map<String, String>,
)
