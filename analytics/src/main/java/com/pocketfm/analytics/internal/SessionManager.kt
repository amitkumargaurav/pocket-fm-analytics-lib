package com.pocketfm.analytics.internal

import java.util.UUID

internal class SessionManager {
    @Volatile
    private var sessionId: String = UUID.randomUUID().toString()

    fun currentSessionId(): String = sessionId

    fun rotateSession() {
        sessionId = UUID.randomUUID().toString()
    }
}
