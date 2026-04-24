package com.pocketfm.analytics.internal

import com.pocketfm.analytics.AnalyticsEvent
import com.pocketfm.analytics.AnalyticsEventType
import java.util.UUID

internal class EventFactory(
    private val identityManager: IdentityManager,
    private val sessionManager: SessionManager,
) {
    fun create(
        type: AnalyticsEventType,
        screenName: String?,
        metadata: Map<String, String>,
    ): AnalyticsEvent {
        return AnalyticsEvent(
            eventId = UUID.randomUUID().toString(),
            type = type,
            clientTimestampMs = System.currentTimeMillis(),
            sessionId = sessionManager.currentSessionId(),
            anonymousId = identityManager.anonymousId(),
            userId = identityManager.userId(),
            screenName = screenName,
            metadata = metadata,
        )
    }
}
