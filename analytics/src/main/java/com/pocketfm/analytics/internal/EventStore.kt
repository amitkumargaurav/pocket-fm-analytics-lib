package com.pocketfm.analytics.internal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pocketfm.analytics.AnalyticsEvent
import com.pocketfm.analytics.AnalyticsEventType

internal class EventStore(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE events (
                event_id TEXT PRIMARY KEY,
                event_type TEXT NOT NULL,
                client_timestamp_ms INTEGER NOT NULL,
                session_id TEXT NOT NULL,
                anonymous_id TEXT NOT NULL,
                user_id TEXT,
                screen_name TEXT,
                metadata_json TEXT NOT NULL,
                retry_count INTEGER NOT NULL DEFAULT 0,
                created_at_ms INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX idx_events_created_at ON events(created_at_ms)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun enqueue(event: AnalyticsEvent) {
        val values = ContentValues().apply {
            put("event_id", event.eventId)
            put("event_type", event.type.name)
            put("client_timestamp_ms", event.clientTimestampMs)
            put("session_id", event.sessionId)
            put("anonymous_id", event.anonymousId)
            put("user_id", event.userId)
            put("screen_name", event.screenName)
            put("metadata_json", JsonUtil.metadataToJson(event.metadata))
            put("created_at_ms", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("events", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun nextBatch(limit: Int): List<AnalyticsEvent> {
        val cursor = readableDatabase.query(
            "events",
            arrayOf(
                "event_id",
                "event_type",
                "client_timestamp_ms",
                "session_id",
                "anonymous_id",
                "user_id",
                "screen_name",
                "metadata_json",
            ),
            null,
            null,
            null,
            null,
            "created_at_ms ASC",
            limit.toString(),
        )
        return cursor.use {
            val results = mutableListOf<AnalyticsEvent>()
            while (it.moveToNext()) {
                results += AnalyticsEvent(
                    eventId = it.getString(0),
                    type = AnalyticsEventType.valueOf(it.getString(1)),
                    clientTimestampMs = it.getLong(2),
                    sessionId = it.getString(3),
                    anonymousId = it.getString(4),
                    userId = it.getString(5),
                    screenName = it.getString(6),
                    metadata = JsonUtil.metadataFromJson(it.getString(7)),
                )
            }
            results
        }
    }

    fun delete(eventIds: List<String>) {
        if (eventIds.isEmpty()) return
        val placeholders = eventIds.joinToString(",") { "?" }
        writableDatabase.delete("events", "event_id IN ($placeholders)", eventIds.toTypedArray())
    }

    companion object {
        private const val DB_NAME = "pfm_analytics.db"
        private const val DB_VERSION = 1
    }
}
