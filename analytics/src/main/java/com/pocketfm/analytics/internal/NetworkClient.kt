package com.pocketfm.analytics.internal

import com.pocketfm.analytics.AnalyticsConfig
import com.pocketfm.analytics.AnalyticsEvent
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal class NetworkClient(private val config: AnalyticsConfig) {

    fun register(packageName: String): Boolean {
        return runCatching {
            val payload = JSONObject()
                .put("package_name", packageName)
                .put("sdk", "pocketfm-analytics-android")
            post(config.registerUrl, payload.toString(), null)
        }.getOrElse {
            AnalyticsLogger.e("Registration failed", it)
            false
        }
    }

    fun sendBatch(events: List<AnalyticsEvent>): Boolean {
        if (events.isEmpty()) return true
        return runCatching {
            val eventJsonArray = JSONArray()
            events.forEach { event ->
                eventJsonArray.put(
                    JSONObject()
                        .put("event_id", event.eventId)
                        .put("event_type", event.type.name.lowercase())
                        .put("client_timestamp_ms", event.clientTimestampMs)
                        .put("session_id", event.sessionId)
                        .put("anonymous_id", event.anonymousId)
                        .put("user_id", event.userId)
                        .put("screen_name", event.screenName)
                        .put("metadata", JSONObject(event.metadata)),
                )
            }
            val payload = JSONObject().put("events", eventJsonArray)
            post(config.ingestUrl, payload.toString(), events.joinToString(",") { it.eventId })
        }.getOrElse {
            AnalyticsLogger.e("Sending batch failed", it)
            false
        }
    }

    private fun post(url: String, body: String, idempotencyKey: String?): Boolean {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            if (!idempotencyKey.isNullOrBlank()) {
                setRequestProperty("Idempotency-Key", idempotencyKey)
            }
        }

        return connection.use {
            it.outputStream.use { stream -> stream.write(body.toByteArray(Charsets.UTF_8)) }
            val code = it.responseCode
            AnalyticsLogger.d("Network response code=$code")
            code in 200..299
        }
    }

    private inline fun <T : HttpURLConnection?, R> T.use(block: (T) -> R): R {
        return try {
            block(this)
        } finally {
            this?.disconnect()
        }
    }
}
