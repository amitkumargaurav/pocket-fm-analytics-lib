package com.pocketfm.analytics.internal

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pocketfm.analytics.AnalyticsConfig

internal class DispatchWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val config = StoredConfigStore(applicationContext).read() ?: return Result.success()
        val store = EventStore(applicationContext)
        val networkClient = NetworkClient(config)
        val events = store.nextBatch(config.maxBatchSize)
        if (events.isEmpty()) return Result.success()

        val sent = networkClient.sendBatch(events)
        return if (sent) {
            store.delete(events.map { it.eventId })
            Result.success()
        } else {
            Result.retry()
        }
    }
}
