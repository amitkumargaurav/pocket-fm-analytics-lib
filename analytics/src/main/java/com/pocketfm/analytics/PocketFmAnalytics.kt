package com.pocketfm.analytics

import android.content.Context
import com.pocketfm.analytics.internal.AnalyticsLogger
import com.pocketfm.analytics.internal.Dispatcher
import com.pocketfm.analytics.internal.EventFactory
import com.pocketfm.analytics.internal.EventStore
import com.pocketfm.analytics.internal.IdentityManager
import com.pocketfm.analytics.internal.NetworkClient
import com.pocketfm.analytics.internal.SessionManager
import com.pocketfm.analytics.internal.StoredConfigStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object PocketFmAnalytics {
    @Volatile
    private var initialized = false

    private lateinit var eventFactory: EventFactory
    private lateinit var eventStore: EventStore
    private lateinit var dispatcher: Dispatcher
    private lateinit var identityManager: IdentityManager
    private lateinit var networkClient: NetworkClient

    private val screenStartMap = ConcurrentHashMap<String, Long>()
    private val ioExecutor = Executors.newSingleThreadExecutor()

    @Synchronized
    fun init(context: Context, config: AnalyticsConfig) {
        if (initialized) return

        val appContext = context.applicationContext
        AnalyticsLogger.configure(config.debugLoggingEnabled)

        val configStore = StoredConfigStore(appContext)
        configStore.save(config)

        identityManager = IdentityManager(appContext)
        val sessionManager = SessionManager()
        eventFactory = EventFactory(identityManager, sessionManager)
        eventStore = EventStore(appContext)
        networkClient = NetworkClient(config)
        dispatcher = Dispatcher(appContext, config.flushIntervalMinutes)

        ioExecutor.execute {
            val registered = networkClient.register(appContext.packageName)
            AnalyticsLogger.d("App registered=$registered")
            dispatcher.schedulePeriodic()
            dispatcher.triggerNow()
        }

        initialized = true
    }

    fun identify(userId: String) {
        ensureInitialized()
        identityManager.identify(userId)
    }

    fun clearUser() {
        ensureInitialized()
        identityManager.clearUser()
    }

    fun trackImpression(
        screenName: String,
        itemName: String,
        order: Int,
        orientation: String,
        metadata: Map<String, String> = emptyMap(),
    ) {
        ensureInitialized()
        val payload = LinkedHashMap<String, String>()
        payload["item_name"] = itemName
        payload["order"] = order.toString()
        payload["orientation"] = orientation
        payload.putAll(metadata)
        enqueueEvent(AnalyticsEventType.IMPRESSION, screenName, payload)
    }

    fun startScreen(screenName: String, metadata: Map<String, String> = emptyMap()) {
        ensureInitialized()
        screenStartMap[screenName] = System.currentTimeMillis()
        if (metadata.isNotEmpty()) {
            enqueueEvent(
                AnalyticsEventType.SCREEN_TIME,
                screenName,
                metadata + mapOf("phase" to "start"),
            )
        }
    }

    fun endScreen(screenName: String, metadata: Map<String, String> = emptyMap()) {
        ensureInitialized()
        val startedAt = screenStartMap.remove(screenName) ?: System.currentTimeMillis()
        val durationMs = (System.currentTimeMillis() - startedAt).coerceAtLeast(0)
        enqueueEvent(
            AnalyticsEventType.SCREEN_TIME,
            screenName,
            metadata + mapOf("duration_ms" to durationMs.toString(), "phase" to "end"),
        )
    }

    fun trackClick(
        screenName: String,
        targetName: String,
        metadata: Map<String, String> = emptyMap(),
    ) {
        ensureInitialized()
        enqueueEvent(
            AnalyticsEventType.CLICK,
            screenName,
            metadata + mapOf("target_name" to targetName),
        )
    }

    fun flush() {
        ensureInitialized()
        dispatcher.triggerNow()
    }

    private fun enqueueEvent(type: AnalyticsEventType, screenName: String?, metadata: Map<String, String>) {
        ioExecutor.execute {
            val event = eventFactory.create(type, screenName, metadata)
            eventStore.enqueue(event)
            dispatcher.triggerNow()
        }
    }

    private fun ensureInitialized() {
        check(initialized) { "PocketFmAnalytics.init must be called first" }
    }
}
